package pl.pw.cyber.dbaccess.testing.dsl.abilities

trait EntropyCalculateAbility {

    double shannonEntropy(String input) {
        if (!input) return 0.0
        def freqMap = input.toList().countBy { it }
        int len = input.length()

        return freqMap.values().stream()
                .mapToDouble { count ->
                    double p = count / (double) len
                    return -p * (Math.log(p) / Math.log(2))
                }
                .sum() * len
    }
}
