// Question 2
class Critter {
    // declaring variables
    String name;
    Critter(String gname) {name = gname;}

    // poke method
    public void poke() {
        String name = "";
        System.out.println(this.name + " was poked.");
    }

    // eat method
    public void eat(Critter critter) {
        System.out.println(name + " ate " + critter.name + ".");
    }
    public static void main(String[] args) {
        // object instantiation
        Critter critter = new Critter("Cow");
        Critter victim = new Critter("Victim");
        // calling methods
        critter.eat(victim);
        critter.poke();
        critter.eat(critter); // ideally this is a different critter
    }
}


