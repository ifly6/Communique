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
package com.git.ifly6.marconi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter;
import com.git.ifly6.javatelegram.JavaTelegram;
import com.git.ifly6.javatelegram.util.JInfoFetcher;
import com.git.ifly6.nsapi.NSNation;

/**
 * @author Kevin
 *
 */
public class MarconiRecruiter extends AbstractCommuniqueRecruiter {

	private Marconi marconi;
	private Thread thread;
	private Set<String> flagsSet;

	/**
	 * @param marconi
	 */
	public MarconiRecruiter(Marconi marconi) {
		this.marconi = marconi;
	}

	/**
	 * @see com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter#send()
	 */
	@Override public void send() {

		Runnable runner = new Runnable() {
			@Override public void run() {

				boolean isSending = true;
				while (isSending) {

					String[] recipients = new String[] {};
					recipients = new JInfoFetcher().getNew();

					String intendedRecipient = null;
					for (String element : recipients) {
						if (!sentList.contains(element) && !MarconiRecruiter.this.isProscribed(element)) {
							intendedRecipient = element;
							break;
						}
					}

					// Otherwise, start sending.
					JavaTelegram client = new JavaTelegram(marconi);
					client.setKeys(marconi.exportState().keys);
					client.setRecipients(new String[] { intendedRecipient });
					client.connect();

					try {
						Thread.sleep(180 * 1000);
					} catch (InterruptedException e) {
						// nothing, since it cannot be interrupted.
					}

				}
			}
		};

		thread = new Thread(runner);
		thread.start();
	}

	private boolean isProscribed(String nationName) {

		if (flagsSet == null) {
			String[] recipients = marconi.exportState().recipients;
			flagsSet = new HashSet<>();
			for (String element : recipients) {
				if (element.startsWith("flags:recruit -- region:")) {
					flagsSet.add(element);
				}
			}
		}

		try {

			String region = new NSNation(nationName).populateData().getRegion();
			for (String excludeRegion : flagsSet) {
				if (excludeRegion.replace(" ", "_").equalsIgnoreCase(region.replace(" ", "_"))) { return true; }
			}

		} catch (IOException e) {
			// do nothing and fall to return false
		}

		return false;
	}

}
