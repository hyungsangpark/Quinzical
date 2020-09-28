package quinzical.Questions;

public class Question {

    private String _clue;
    private String[] _answer; // string array of answers String[]
    private int _lineNumber;
    private Category _parent;
    private String _whatIsThis;
    private int _value;
    // category parent

    // String[] pass into || _parent = parent; Category parent
    public Question(String clue, String[] answer, Category parent, int value) {
        _clue = clue;
        _answer = answer;
        _parent = parent;
        _value = value;
    }

    public int getLineNumber() {
        return _lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this._lineNumber = lineNumber;
    }

    public String get_clue() {
        return _clue;
    }

    public void set_clue(String _clue) {
        this._clue = _clue;
    }

    public String[] get_answer() {
        return _answer;
    }

    public Category getParent() {
        return _parent;
    }

    public void set_whatIsThis(String _whatIsThis) {
        this._whatIsThis = _whatIsThis;
    }

    public String get_whatIsThis() {
        return _whatIsThis;
    }

    public int getValue() {
        return _value;
    }
}
