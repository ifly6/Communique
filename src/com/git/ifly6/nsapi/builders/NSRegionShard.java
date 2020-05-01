package com.git.ifly6.nsapi.builders;

public enum NSRegionShard {

	REGION {
		@Override
		public String toString() {
			return "region=";
		}
	},

	PROPER_NAME {
		@Override
		public String toString() {
			return "name";
		}
	},

	DELEGATE {
		@Override
		public String toString() {
			return "delegate";
		}
	},

	FOUNDER {
		@Override
		public String toString() {
			return "founder";
		}
	},

	NATIONS_LIST {
		@Override
		public String toString() {
			return "nations";
		}
	}

}
