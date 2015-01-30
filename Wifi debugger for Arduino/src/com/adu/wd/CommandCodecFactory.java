package com.adu.wd;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * @author 최의신
 *
 */
public class CommandCodecFactory implements ProtocolCodecFactory
{
	private ProtocolEncoder encoder;
    private ProtocolDecoder decoder;
    
    
    public CommandCodecFactory()
    {
    	encoder = new CommandEncoder();
    	decoder = new CommandDecoder();
    }
    
	@Override
	public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
		return encoder;
	}

}
