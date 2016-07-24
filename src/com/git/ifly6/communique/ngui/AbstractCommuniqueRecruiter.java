/* Copyright (c) 2016 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package com.git.ifly6.communique.ngui;

import java.util.Arrays;
import java.util.List;

import com.git.ifly6.communique.io.CConfig;

/**
 * @author Kevin
 *
 */
public abstract class AbstractCommuniqueRecruiter {

	private List<String> recipients;
	private List<String> sentList;

	public void setWithCConfig(CConfig config) {

		setClientKey(config.keys.getClientKey());
		setSecretKey(config.keys.getSecretKey());
		setTelegramId(config.keys.getTelegramId());

		recipients = Arrays.asList(config.recipients);
		sentList = Arrays.asList(config.sentList);

	}

	public abstract void setClientKey(String key);

	public abstract void setSecretKey(String key);

	public abstract void setTelegramId(String id);

	public abstract void send();

}