package com.git.ifly6.nsapi.builders;

public enum NSNationShard {

	NATION {
		@Override
		public String toString() {
			return "nation=";
		}
	},

	PROPER_NAME {
		@Override
		public String toString() {
			return "name";
		}
	},

	REGION {
		@Override
		public String toString() {
			return "region";
		}
	},

	CATEGORY {
		@Override
		public String toString() {
			return "category";
		}
	},

	ENDORSEMENT_LIST {
		@Override
		public String toString() {
			return "endorsements";
		}
	},

	CAN_RECRUIT {
		@Override
		public String toString() {
			return "tgcanrecruit";
		}
	},

	CAN_CAMPAIGN {
		@Override
		public String toString() {
			return "tgcancampaign";
		}
	},

	CENSUS {
		@Override
		public String toString() {
			return "census";
		}
	}

}
