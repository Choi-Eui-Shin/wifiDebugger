package com.adu.wd;

/**
 * @author 최의신
 *
 */
public class NumberData extends BaseData
{
	private long longValue;
	private double doubleValue;

	public NumberData(String ch, long l)
	{
		super(ch);
		this.longValue = l;
	}
	
	public NumberData(String ch, double d)
	{
		super(ch);
		this.doubleValue = d;
	}
	
	public NumberData(String ch, long l, double d)
	{
		super(ch);
		this.longValue = l;
		this.doubleValue = d;
	}
	
	public long getLongValue() {
		return longValue;
	}
	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}
	public double getDoubleValue() {
		return doubleValue;
	}
	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}
}
