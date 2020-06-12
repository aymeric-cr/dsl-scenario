import org.codehaus.groovy.control.CompilationFailedException

public class MaClasseGroovy {

    public static void main(String[] args) {

        GroovyShell shell = new GroovyShell();
        Script script;
        try {
            // Chargement du script groovy
            script = shell.parse(new File("scripts/exemple1.gy"));
            Binding binding = new Binding();
            // Création d'un paramètre
            binding.setVariable("argument", "Saint Nicolas");
            script.setBinding(binding);

            // Exécution du script
            Object retour = script.run();
            // Affichage de la valeur de retour du script
            System.out.println(retour);

        } catch (CompilationFailedException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
}