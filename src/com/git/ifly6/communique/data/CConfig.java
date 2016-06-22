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
package com.git.ifly6.communique.data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.git.ifly6.communique.CommuniqueParser;
import com.git.ifly6.javatelegram.JTelegramKeys;

/**
 * <code>CConfig</code> creates a unified object for the storage and retrieval of the entire state of a Communiqué
 * application.
 *
 * <p>
 * Because it is needed to be able to send all the Communiqué flags and relevant assorted information as a single
 * object, this object was created as an integrated system to do so. This program also contains methods to access the
 * interior components of this class using a <code>Map</code> for cross-interoperability with
 * <code>{@link com.git.ifly6.communique.io.CLoader CLoader}</code>,
 * <code>{@link com.git.ifly6.communique.io.CReader CReader}</code>, and
 * <code>{@link com.git.ifly6.communique.io.CWriter CWriter}</code>, which are based on the Java properties file system.
 * Also, the widespread use of reflection in dealing with a <code>Map{@code <String, String>}</code> will allow for
 * greater extensibility over time and significantly less human error in providing methods to access such data.
 * </p>
 *
 */
public class CConfig {

	// For reflection in CLoader to work, these MUST be the only fields
	// For backwards compatibility, these names cannot be changed

	public int version;

	public boolean isRecruitment;
	public boolean isRandomised;
	public boolean isDelegatePrioritised;

	public JTelegramKeys keys;

	public String[] recipients;
	public String[] sentList;

	public void defaultVersion() {
		this.version = CommuniqueParser.version;
	}

	public Map<String, String> produceMap() {

		Map<String, String> preferenceMap = new HashMap<>();

		Class<? extends CConfig> instanceClass = this.getClass();
		for (Field theField : instanceClass.getDeclaredFields()) {

			try {
				if (theField.getType().isArray()) {
					if (theField.getType().getComponentType() == String.class) {

						StringBuilder arrayString = new StringBuilder();
						String[] array = (String[]) theField.get(this);

						arrayString.append(array[0]);
						for (int x = 1; x < array.length; x++) {
							arrayString.append("," + array[x]);
						}

						preferenceMap.put(theField.getName(), arrayString.toString());

					}

				} else {
					preferenceMap.put(theField.getName(), theField.get(this).toString());
				}

			} catch (IllegalArgumentException e) {
				e.printStackTrace();

			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return preferenceMap;

	}

	public void importMap(Map<String, String> info) {

		Class<? extends CConfig> instanceClass = this.getClass();
		Field[] fields = instanceClass.getDeclaredFields();

		try {
			for (Field theField : fields) {

				String value = info.get(theField.getName());
				if (theField.getType().isArray()) {

					if (theField.getType().getComponentType() == String.class) {
						theField.set(this, value.split(","));
					}

				} else {

					if (theField.getType() == JTelegramKeys.class) {
						JTelegramKeys keys = new JTelegramKeys();
						keys.setKeys(value.split(", "));
						theField.set(this, keys);

					} else {
						theField.set(theField.getClass(), value);
					}

				}
			}

		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
