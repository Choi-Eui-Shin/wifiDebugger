package com.adu.wd;

/**
 * @author 최의신
 *
 */
public class StringData extends BaseData
{
	private String stringData;
	
	public StringData(String ch, String s)
	{
		super(ch);
		this.stringData = s;
	}
	
	public String getStringData() {
		return stringData;
	}

	public void setStringData(String stringData) {
		this.stringData = stringData;
	}
}
