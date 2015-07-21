package io.blinktech.wifip2pserver;

/**
 * Created by mayank on 7/15/15.
 */
public class Medicine {

    private String _name, _mg, _expDate, _openDate, _noTabs, _patientId;
    private int _id;

    public Medicine(int _id, String _name, String _mg, String _expDate, String _openDate, String _noTabs, String _patientId) {
        this._id = _id;
        this._name = _name;
        this._mg = _mg;
        this._expDate = _expDate;
        this._openDate = _openDate;
        this._noTabs = _noTabs;
        this._patientId = _patientId;
    }

    public int get_id() { return _id; }

    public String get_name() { return _name; }

    public String get_mg() {
        return _mg;
    }

    public String get_expDate() {
        return _expDate;
    }

    public String get_openDate() {
        return _openDate;
    }

    public String get_noTabs() {
        return _noTabs;
    }

    public String get_patientId() {
        return _patientId;
    }
}