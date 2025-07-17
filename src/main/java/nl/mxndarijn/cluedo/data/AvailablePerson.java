package nl.mxndarijn.cluedo.data;

import java.util.Optional;

public enum AvailablePerson {
    EXECUTOR("ex", "Uitvoerder"),
    SELECTED_PERSON_1("s0", "Eerstgekozene"),
    SELECTED_PERSON_2("s1", "Tweedekozene");

    private final String type;
    private final String name;
    AvailablePerson(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public static Optional<AvailablePerson> getByType(String type) {
        for (AvailablePerson value : values()) {
            if(value.type.equalsIgnoreCase(type))
                return Optional.of(value);
        }
        return Optional.empty();
    }

    public String getName() {
        return name;
    }
}
