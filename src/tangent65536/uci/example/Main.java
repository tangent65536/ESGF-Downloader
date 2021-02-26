package tangent65536.uci.example;

import java.io.File;
import java.util.function.Function;

import tangent65536.uci.ESGFDownloader;

public class Main 
{
	public static void main(String[] args) throws Exception
	{
		final File dir = new File("./CESM1-WACCM-SC/Present/");
		
		Function<Integer, String> modelDates = (i) -> String.format("%04d04-%04d05", i, i + 1);
		
		ESGFDownloader downloader = new ESGFDownloader("esgf-data.ucar.edu", "esg_dataroot/CMIP6", "PAMIP", "NCAR", "CESM1-WACCM-SC", "pdSST-pdSICSIT", null, 1, 1, 1, "AERmonZ", "ua", "gn", "v20201012", 1, 300, modelDates, 8);
		downloader.download(dir);
	}
}
