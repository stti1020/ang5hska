package at.oenb.infrastructure;

import at.oenb.dltrouting.domain.BIC;
import at.oenb.dltrouting.domain.Bank;
import org.apache.commons.text.RandomStringGenerator;

public class GeneratorUtil {

    private static final RandomStringGenerator RANDOM_BIC_GENERATOR =
            new RandomStringGenerator.Builder().withinRange('A', 'Z').build();

    public static String generateRandomBic() {
        return RANDOM_BIC_GENERATOR.generate(8);
    }

    public static BIC generateRandomBIC() {
        return new BIC(generateRandomBic());
    }

    public static Bank generateRandomBank() {
        BIC bic = generateRandomBIC();
        return Bank.builder()
                .bic(bic)
                .name("Random Bank " + bic.getValue())
                .build();
    }
}
