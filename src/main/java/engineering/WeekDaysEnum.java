package engineering;

public enum WeekDaysEnum {
    MONDAY {
        @Override
        public int getDayNum() {
            return 1;
        }
    },

    TUESDAY {
        @Override
        public int getDayNum() {
            return 2;
        }
    },

    WEDNESDAY {
        @Override
        public int getDayNum() {
            return 3;
        }
    },

    THURSDAY {
        @Override
        public int getDayNum() {
            return 4;
        }
    },

    FRIDAY {
        @Override
        public int getDayNum() {
            return 5;
        }
    },

    SATURDAY {
        @Override
        public int getDayNum() {
            return 6;
        }
    },

    SUNDAY {
        @Override
        public int getDayNum() {
            return 7;
        }
    };

    public abstract int getDayNum();

    public static WeekDaysEnum fromString(String weekDayString) {
        switch (weekDayString) {
            case "понедельник":
                return MONDAY;
            case "вторник":
                return TUESDAY;
            case "сред":
                return WEDNESDAY;
            case "четверг":
                return THURSDAY;
            case "пятниц":
                return FRIDAY;
            case "суббот":
                return SATURDAY;
            case "воскресен":
                return SUNDAY;
            default:
                return null;
        }
    }

    public MyDate getNearlyDate(MyDate currentDate) {
        int weekDay = currentDate.getWeekDay();
        if (weekDay == this.getDayNum()) {
            return currentDate;
        }
        if (weekDay < this.getDayNum()) {
            return currentDate.minusDays(7 - (this.getDayNum() - weekDay));
        }
        return currentDate.minusDays(weekDay - this.getDayNum());
    }
}
