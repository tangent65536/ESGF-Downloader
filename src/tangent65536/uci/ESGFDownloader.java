package tangent65536.uci;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

/**
 * @author Copyright (c) 2021, Tangent65536.
 *
 * Do NOT use this program to do bad stuff.
 */

public class ESGFDownloader
{
	/**
	 * "Node" on ESGF.
	 */
	public final String server;
	
	/**
	 * "Activity" on ESGF.
	 */
	public final String activity;
	
	/**
	 * Institution ID on ESGF.
	 */
	public final String institution;
	
	/**
	 * "Source ID" on ESGF.
	 */
	public final String source;
	
	/**
	 * "Experiment ID" on ESGF.
	 */
	public final String experiment;
	
	/**
	 * "Sub-Experiment" on ESGF. Can be null.
	 */
	public final String sub;
	
	/**
	 * rx"ix"pxfx
	 */
	public final int label_i;
	
	/**
	 * rxix"px"fx
	 */
	public final int label_p;
	
	/**
	 * rxixpx"fx"
	 */
	public final int label_f;
	
	/**
	 * "Table ID" with certain flags (such as zonal average) on ESGF.
	 */
	public final String table;
	
	/**
	 * "Variable" on ESGF.
	 */
	public final String variable;
	
	/**
	 * "Grid Label" on ESGF.
	 */
	public final String grid_label;
	
	/**
	 * Version and date information in the URL only.
	 */
	public final String version;
	
	/**
	 * The starting run ID for download.
	 */
	public final int runMin;
	
	/**
	 * The ending run ID for download.
	 */
	public final int runMax;
	
	/**
	 * Lambda function to acquire model filename format for starting and ending date. (Input: integer ; output: String)
	 */
	public final Function<Integer, String> dateFormat;
	
	/**
	 * Max attempts if the download process fails for a file.
	 */
	public final int maxAttempts;
	
	/**
	 * @param node Server node
	 * @param act Activity
	 * @param inst Institution ID
	 * @param src Source ID
	 * @param expr Experiment ID
	 * @param i i
	 * @param p p
	 * @param f f
	 * @param tabl Table ID with certain flags (such as zonal average)
	 * @param var Variable
	 * @param grid Grid Label
	 * @param ver Version ID in URL only
	 * @param rMin Starting run ID
	 * @param rMax Ending run ID
	 * @param enc Model filename format for starting and ending date
	 * @param attempt Max attempts if the download process fails for a file
	 */
	public ESGFDownloader(String node, String act, String inst, String src, String expr, String sub_, int i, int p, int f, String tabl, String var, String grid, String ver, int rMin, int rMax, Function<Integer, String> enc, int attempt)
	{
		this.server = node;
		this.activity = act;
		this.institution = inst;
		this.source = src;
		this.experiment = expr;
		this.sub = sub_;
		this.label_i = i;
		this.label_p = p;
		this.label_f = f;
		this.table = tabl;
		this.variable = var;
		this.grid_label = grid;
		this.version = ver;
		this.runMin = rMin;
		this.runMax = rMax;
		this.dateFormat = enc;
		this.maxAttempts = attempt;
	}
	
	/**
	 * @param dir Output directory
	 */
	public void download(File dir) throws Exception
	{
		URL url;
		String file, sUrl;
		int len, i, j;
		
		InputStream is;
		FileOutputStream fos;
		final byte[] cache = new byte[4096];
		
		String modelDates;
		
		dir.mkdirs();
		
		for(i = this.runMin ; i <= this.runMax ; i++)
		{
			modelDates = this.dateFormat.apply(i);

			if(this.sub == null)
			{
				file = String.format("%s_%s_%s_%s_r%di%dp%df%d_%s_%s.nc", this.variable, this.table, this.source, this.experiment, i, this.label_i, this.label_p, this.label_f, this.grid_label, modelDates);
				sUrl = String.format("https://%s/thredds/fileServer/esg_dataroot/CMIP6/%s/%s/%s/%s/r%di%dp%df%d/%s/%s/%s/%s/%s", this.server, this.activity, this.institution, this.source, this.experiment, i, this.label_i, this.label_p, this.label_f, this.table, this.variable, this.grid_label, this.version, file);
			}
			else
			{
				file = String.format("%s_%s_%s_%s_%s-r%di%dp%df%d_%s_%s.nc", this.variable, this.table, this.source, this.experiment, this.sub, i, this.label_i, this.label_p, this.label_f, this.grid_label, modelDates);
				sUrl = String.format("https://%s/thredds/fileServer/esg_dataroot/CMIP6/%s/%s/%s/%s/%s-r%di%dp%df%d/%s/%s/%s/%s/%s", this.server, this.activity, this.institution, this.source, this.experiment, this.sub, i, this.label_i, this.label_p, this.label_f, this.table, this.variable, this.grid_label, this.version, file);
			}
			
			// System.out.println(sUrl);
			
			for(j = 0 ; j < this.maxAttempts ; j++)
			{
				try
				{
					url = new URL(sUrl);
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setConnectTimeout(30000);
					
					if((len = conn.getResponseCode()) / 100 == 2)
					{
						System.out.println(String.format("Downloading file with run ID = %d ...", i));
						System.out.flush();
						
						is = conn.getInputStream();
						fos = new FileOutputStream(new File(dir, file));
						while((len = is.read(cache)) >= 0)
						{
							fos.write(cache, 0, len);
						}
						fos.close();
						is.close();
						
						System.out.println("Done!");
						System.out.flush();
						
						break;
					}
					else
					{
						System.err.println(String.format("Failed to download file with index = %d (ERR CODE %d).", i, len));
					}
					
					conn.disconnect();
				}
				catch(Exception e)
				{
					System.err.println(String.format("Failed to download file with index = %d (EXCEPTION).", i));
					e.printStackTrace();
				}
			}
		}
	}
	
	private static final HashMap<String, String> ARGUMENTS = new HashMap<>();
	
	private static final ArrayList<String> REQUIRED_ARGS = new ArrayList<>();
	private static final HashSet<String> RECOGNIZED_ARGS = new HashSet<>();
	
	static
	{
		REQUIRED_ARGS.add("dir");
		REQUIRED_ARGS.add("node");
		REQUIRED_ARGS.add("act");
		REQUIRED_ARGS.add("inst");
		REQUIRED_ARGS.add("src");
		REQUIRED_ARGS.add("expr");
		REQUIRED_ARGS.add("i");
		REQUIRED_ARGS.add("p");
		REQUIRED_ARGS.add("f");
		REQUIRED_ARGS.add("tabl");
		REQUIRED_ARGS.add("var");
		REQUIRED_ARGS.add("grid");
		REQUIRED_ARGS.add("ver");
		REQUIRED_ARGS.add("runs");
		REQUIRED_ARGS.add("datf");
		
		RECOGNIZED_ARGS.add("sub");
		RECOGNIZED_ARGS.add("attempt");
		
		ARGUMENTS.put("dir", "<output dir>         Output directory to store the files.");
		ARGUMENTS.put("node", "<domain>            Server that the program should download data from.");
		ARGUMENTS.put("act", "<activity ID>        Activity ID.");
		ARGUMENTS.put("inst", "<institution ID>    Institution ID.");
		ARGUMENTS.put("src", "<source ID>          Source ID.");
		ARGUMENTS.put("expr", "<experiment ID>     Experiment ID.");
		ARGUMENTS.put("sub", "<sub-experiment>     Sub-experiment.");
		ARGUMENTS.put("i", "<var-label i>          Variant label 'i'.");
		ARGUMENTS.put("p", "<var-label p>          Variant label 'p'.");
		ARGUMENTS.put("f", "<var-label f>          Variant label 'f'.");
		ARGUMENTS.put("tabl", "<table ID>          Table ID.");
		ARGUMENTS.put("var", "<variable>           Variable.");
		ARGUMENTS.put("grid", "<grid-label>        Grid label.");
		ARGUMENTS.put("ver", "<version>            File version ID in the URL.");
		ARGUMENTS.put("runs", "<min>,<max>         Runs that should be downloaded.");
		ARGUMENTS.put("datf", "<scheme>,<fmt>      Schemes and expression for model output dates.");
		ARGUMENTS.put("attempt", "<max attempts>   Maximum attempts when downloading process fails\n                             for a file. Default is 8.");
	}

	public static void main(String[] args) throws Exception
	{
		int i,
		    label_i = 0,
		    label_p = 0,
		    label_f = 0,
		    rMin = 0,
		    rMax = 0,
		    attempt = 8;
		
		String sCache = null;
		
		Function<Integer, String> dateFmt = null;

		System.out.println();
		System.out.println("        +----------------------------------------------+");
		System.out.println("        | ESGF model output downloader by Tangent65536 |");
		System.out.println("        |      https://github.com/tangent65536         |");
		System.out.println("        +----------------------------------------------+");
		System.out.println();
		System.out.flush();
		
		if(args.length == 0 || (args.length == 1 && args[0].toLowerCase().equals("--help")))
		{
			System.out.println("Required arguments:");
			for(String s : REQUIRED_ARGS)
			{
				System.out.println(String.format("  --%s %s", s, ARGUMENTS.get(s)));
			}
			
			System.out.println();
			System.out.println("Optional arguments:");
			for(String s : RECOGNIZED_ARGS)
			{
				System.out.println(String.format("  --%s %s", s, ARGUMENTS.get(s)));
			}
			
			System.out.println();
			System.out.println("For the --datf parameter, <fmt> is the model output starting-ending date. The");
			System.out.println(" following <scheme>s are available:");
			System.out.println("   0 -> Constant text value identical to <fmt> for every run.");
			System.out.println("   1 -> Value varies along with run ID, with starting and ending year expressed");
			System.out.println("         as the run ID. E.g. for run ID equals to 232, the model year is 0232.");
			System.out.println("         In this case, say the simulation period is from January to December,");
			System.out.println("         the <fmt> option should be \"%04d01-%04d12\".");
			System.out.println("   2 -> Value varies along with run ID, with starting year expressed as the run");
			System.out.println("         ID, and ending expressed as run ID + 1. E.g. for run ID equals to 232,");
			System.out.println("         the model year is 0232. In this case, say the simulation period is");
			System.out.println("         from January to December, the <fmt> option should be \"%04d01-%04d12\".");
			System.out.println();
			return;
		}
		
		for(i = 0 ; i < args.length ; i++)
		{
			if(sCache == null)
			{
				if(args[i].startsWith("--"))
				{
					args[i] = args[i].substring(2).toLowerCase();
					if(REQUIRED_ARGS.remove(args[i]) || RECOGNIZED_ARGS.remove(args[i]))
					{
						sCache = args[i];
					}
					else
					{
						System.err.println(String.format("Unrecognized parameter '--%s'.", args[i]));
						return;
					}
				}
				else
				{
					System.err.println(String.format("Unrecognized parameter '%s'.", args[i]));
					return;
				}
			}
			else
			{
				ARGUMENTS.put(sCache, args[i]);
				sCache = null;
			}
		}
		
		if(sCache != null)
		{
			System.err.println(String.format("Invalid parameter assignment for '--%s'.", sCache));
			return;
		}
		
		if(!REQUIRED_ARGS.isEmpty())
		{
			System.err.println("The following required parameter(s) are missing:");
			
			for(String s : REQUIRED_ARGS)
			{
				System.err.println(String.format("  --%s %s", s, ARGUMENTS.get(s)));
			}
			
			System.err.println();
			
			return;
		}
		
		for(String s : RECOGNIZED_ARGS)
		{
			ARGUMENTS.remove(s);
		}
		
		
		try
		{
			label_i = Integer.parseInt(ARGUMENTS.get("i"));
			if(label_i <= 0)
			{
				throw new Exception();
			}
		}
		catch(Exception e)
		{
			System.err.println("ERROR: '--i' must specify a positive integer.");
			return;
		}
		
		try
		{
			label_p = Integer.parseInt(ARGUMENTS.get("p"));
			if(label_p <= 0)
			{
				throw new Exception();
			}
		}
		catch(Exception e)
		{
			System.err.println("ERROR: '--p' must specify a positive integer.");
			return;
		}
		
		try
		{
			label_f = Integer.parseInt(ARGUMENTS.get("f"));
			if(label_f <= 0)
			{
				throw new Exception();
			}
		}
		catch(Exception e)
		{
			System.err.println("ERROR: '--f' must specify a positive integer.");
			return;
		}
		
		try
		{
			args = ARGUMENTS.get("runs").split(",", 2);
			rMin = Integer.parseInt(args[0]);
			rMax = Integer.parseInt(args[1]);
			
			if(rMax < rMin || rMin <= 0)
			{
				throw new Exception();
			}
		}
		catch(Exception e)
		{
			System.err.println("ERROR: '--run' must specify 2 positive integers, seperated by a comma without space, with the latter one greater than or equal to the prior one.");
			return;
		}
		
		try
		{
			args = ARGUMENTS.get("datf").split(",", 2);
			final String fmt = args[1];
			
			switch(Integer.parseInt(args[0]))
			{
				case 0:
				{
					dateFmt = (x) -> (fmt);
					break;
				}
				case 1:
				{
					dateFmt = (x) -> (String.format(fmt, x, x));
					break;
				}
				case 2:
				{
					dateFmt = (x) -> (String.format(fmt, x, x + 1));
					break;
				}
				default:
				{
					throw new Exception();
				}
			}
			
			// Test if the <fmt> expression is valid.
			dateFmt.apply(0);
		}
		catch(Exception e)
		{
			System.err.println("ERROR: Invalid '--datf' <scheme> or <fmt>. The <scheme> and <fmt> string must be seperated by a comma without space.");
			return;
		}
		
		if(ARGUMENTS.containsKey("attempt"))
		{
			try
			{
				attempt = Integer.parseInt(ARGUMENTS.get("atempts"));
				if(attempt <= 0)
				{
					throw new Exception();
				}
			}
			catch(Exception e)
			{
				System.err.println("ERROR: '--attempt' must specify a positive integer.");
				return;
			}
		}
		
		ESGFDownloader downloader = new ESGFDownloader(ARGUMENTS.get("node"),
		                                               ARGUMENTS.get("act"),
		                                               ARGUMENTS.get("inst"),
		                                               ARGUMENTS.get("src"),
		                                               ARGUMENTS.get("expr"),
		                                               ARGUMENTS.get("sub"),
		                                               label_i,
		                                               label_p,
		                                               label_f,
		                                               ARGUMENTS.get("tabl"),
		                                               ARGUMENTS.get("var"),
		                                               ARGUMENTS.get("grid"),
		                                               ARGUMENTS.get("ver"),
		                                               rMin,
		                                               rMax,
		                                               dateFmt,
		                                               attempt);
		
		downloader.download(new File(ARGUMENTS.get("dir")));
	}
}
