package modelChecker.views.viewpart;

public class ResultStat {
	private int countMatchingTrans;
	private int countIllegalTransVtoV;
	
	public ResultStat(int countMatchingTrans, int countIllegalTransVtoV) {
		this.countMatchingTrans = countMatchingTrans;
		this.countIllegalTransVtoV = countIllegalTransVtoV;
	}

	public int getCountMatchingTrans() {
		return countMatchingTrans;
	}

	public int getCountIllegalTransVtoV() {
		return countIllegalTransVtoV;
	}
}
