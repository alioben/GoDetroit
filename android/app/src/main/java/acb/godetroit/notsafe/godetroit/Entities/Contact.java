package acb.godetroit.notsafe.godetroit.Entities;

import java.io.Serializable;

public class Contact implements Serializable{
    private String name;
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object that){
        if(that instanceof Contact){
            Contact c = (Contact)that;
            return c.phone.replaceAll(" ", "")
                    .replaceAll("-", "")
                    .replaceAll("\\(", "")
                    .replaceAll("\\)", "").equals(phone);
        }
        return false;
    }
}