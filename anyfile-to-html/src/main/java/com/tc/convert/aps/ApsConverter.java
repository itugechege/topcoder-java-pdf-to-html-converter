package com.tc.convert.aps;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;

public interface ApsConverter {

	/**
	 * return number of pages
	 */
	public int convert(InputStream in, String dirOut, String fileName) throws Exception;
}
