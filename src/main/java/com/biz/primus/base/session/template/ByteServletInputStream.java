package com.biz.primus.base.session.template;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class ByteServletInputStream extends ServletInputStream{
	
	private ByteArrayInputStream abis;
	
	
	public ByteServletInputStream(ByteArrayInputStream abis) {
		super();
		this.abis = abis;
	}

	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReadListener(ReadListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
