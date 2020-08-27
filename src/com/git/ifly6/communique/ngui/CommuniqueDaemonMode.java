/*
 * Copyright (c) 2020 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.communique.ngui;

import com.git.ifly6.nsapi.telegram.util.JInfoFetcher;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum CommuniqueDaemonMode {

	NEW {
		public List<String> get() {
			checkedNow();
			return JInfoFetcher.instance().getNew();
		}

		public List<String> get(String input) {
			if (!input.equals(EMPTY))
				throw new UnsupportedOperationException("New must be called with empty");
			return this.get();
		}

		@Override
		public String toString() {
			return "NEW";
		}
	},

	MOVED_OUT {

		private List<String> last;

		@Override
		public List<String> get() {
			throw new UnsupportedOperationException("Cannot check moves from without region");
//			return Collections.emptyList();
		}

		@Override
		public List<String> get(String input) {
			checkedNow();
			List<String> current = JInfoFetcher.instance().getRegion(input);

			if (Objects.isNull(last)) {
				last = current;
				return Collections.emptyList();

			} else {
				// find all that are in last check which are not in current
				List<String> result = last.stream()
						.filter(s -> !current.contains(s))
						.collect(Collectors.toList());

				last = current;
				return result;
			}
		}

		@Override
		public String toString() {
			return "MOVED_OUT";
		}
	},

	MOVED_IN {
		private List<String> last;

		@Override
		public List<String> get() {
			throw new UnsupportedOperationException("Cannot check moves in without region");
//			return Collections.emptyList();
		}

		@Override
		public List<String> get(String input) {
			checkedNow();
			List<String> current = JInfoFetcher.instance().getRegion(input);

			if (Objects.isNull(last)) {
				last = current;
				return Collections.emptyList();  // we don't know

			} else {
				// find all that are in current check which are not in last
				List<String> result = current.stream()
						.filter(s -> !last.contains(s))
						.collect(Collectors.toList());

				last = current;
				return result;
			}
		}

		@Override
		public String toString() {
			return "MOVED_OUT";
		}
	};

	public static final String EMPTY = "";

	private Instant lastCheck;

	public void checkedNow() {
		this.lastCheck = Instant.now();
	}

	public Instant lastUpdated() {
		return lastCheck;
	}

	public abstract List<String> get();
	public abstract List<String> get(String input);

}
