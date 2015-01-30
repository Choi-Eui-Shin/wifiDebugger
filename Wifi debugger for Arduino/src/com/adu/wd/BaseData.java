package com.adu.wd;

/**
 * @author 최의신
 *
 */
public class BaseData
{
	protected String channel;
	
	public BaseData(String channel)
	{
		this.channel = channel;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}
