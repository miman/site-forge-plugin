package eu.miman.forge.plugin.site.completer;

public enum SiteDocFormatType {
	APT("apt", "APT"), MARKDOWN("markdown", "Markdown");

	private String type;
	private String description;

	private SiteDocFormatType(final String type, final String description) {
		setType(type);
		setDescription(description);
	}

	public String getType() {
		return type;
	}

	private void setType(String type) {
		if (type != null) {
			type = type.trim().toLowerCase();
		}
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	private void setDescription(final String description) {
		this.description = description;
	}

	public static SiteDocFormatType from(String type) {
		SiteDocFormatType result = APT;
		if ((type != null) && !type.trim().isEmpty()) {
			type = type.trim();
			for (SiteDocFormatType p : values()) {
				if (p.getType().equals(type) || p.name().equalsIgnoreCase(type)) {
					result = p;
					break;
				}
			}
		}
		return result;
	}
}
