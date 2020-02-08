package edu.repetita.settings;

import edu.repetita.core.Setting;

import java.util.*;

public class SRSetting extends Setting {

	private int maxSeg = 1;

	public int getMaxSeg() {
		return maxSeg;
	}

	public void setMaxSeg(int maxSeg) {
		this.maxSeg = maxSeg;
	}

	@Override
	public void help(HashMap<String, String> args) {
		args.put("maxseg", "The maximum number of intermediate segments (i.e., source and destination nodes are not counted)");
	}

	@Override
	protected void setExtra(String key, Object value) throws IllegalArgumentException {
		switch (key) {
		case "maxseg":
			if (!(value instanceof Integer)) {
				throw new IllegalArgumentException("The maximum number of intermediate segments should be an integer.");
			}
			int maxSeg = (Integer) value;
			if (maxSeg < 0) {
				throw new IllegalArgumentException("'" + value + "' is not a valid number of segment." +
						" It should be positive.");
			}
			this.maxSeg = maxSeg;
			break;
		default:
			super.setExtra(key, value);
		}
	}

	@Override
	public Setting clone() {
		Setting copy = new SRSetting();
		this.init(copy);
		return copy;
	}

	@Override
	protected void init(Setting copy) {
		super.init(copy);
		((SRSetting) copy).setMaxSeg(maxSeg);
	}
}
