#!/bin/bash

export JAVA_EXE="${HOME}/workspace/jre8u281x64/bin/java"

export ESGF_NODE="esgf-data.ucar.edu"
export ESGF_CMIP6_PATH="esg_dataroot/CMIP6"
export ESGF_ACTIVITY="PAMIP"
export ESGF_INSTITUTION="NCAR"
export ESGF_SOURCE="CESM1-WACCM-SC"
export ESGF_EXPERIMENT="pdSST-pdSICSIT"
export ESGF_I="1"
export ESGF_P="1"
export ESGF_F="1"
export ESGF_TABLE="Amon"
export ESGF_VARIABLE="psl"
export ESGF_GRID="gn"
export ESGF_VERSION="v20201012"
export ESGF_RUNS="1,300"
export ESGF_DATE_SCHEME="2,%04d04-%04d05"


"${JAVA_EXE}" -jar ./ESGFDownloader.jar \
	--dir "./${ESGF_ACTIVITY}/${ESGF_EXPERIMENT}/${ESGF_SOURCE}/${ESGF_VARIABLE}" \
	--node "${ESGF_NODE}" \
	--path "${ESGF_CMIP6_PATH}" \
	--act "${ESGF_ACTIVITY}" \
	--inst "${ESGF_INSTITUTION}" \
	--src "${ESGF_SOURCE}" \
	--expr "${ESGF_EXPERIMENT}" \
	--i "${ESGF_I}" \
	--p "${ESGF_P}" \
	--f "${ESGF_F}" \
	--tabl "${ESGF_TABLE}" \
	--var "${ESGF_VARIABLE}" \
	--grid "${ESGF_GRID}" \
	--ver "${ESGF_VERSION}" \
	--runs "${ESGF_RUNS}" \
	--datf "${ESGF_DATE_SCHEME}"

