package smartthings.dw.buildinfo;

public class MetadataResponse {
	private String repo;
	private String commit;
	private String tag;
	private String branch;
	private String buildNumber;
	private String gitDescription;

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	public String getGitDescription() {
		return gitDescription;
	}

	public void setGitDescription(String gitDescription) {
		this.gitDescription = gitDescription;
	}
}
