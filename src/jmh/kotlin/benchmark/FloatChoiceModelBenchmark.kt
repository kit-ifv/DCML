package benchmark

import edu.kit.ifv.mobitopp.discretechoice.distribution.FloatCumulateDistributionArray
import edu.kit.ifv.mobitopp.discretechoice.distribution.FloatMultinomialLogitArray
import edu.kit.ifv.mobitopp.discretechoice.models.FloatBatchUtilityChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.selection.FloatSelectionFunctionArray
import edu.kit.ifv.mobitopp.discretechoice.selection.FloatWeightedBinarySelection
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.min
import kotlin.random.Random
import kotlin.system.measureNanoTime

fun main() {
    val alternativeCount = 2_048
    val benchmarkIterations = 1_000_000
    val seed = 42

    val localcharacteristic =
        Impedance.random(size = alternativeCount)

    val random = Random(seed)
    val characteristic = Attributes.random()
    val choiceModel = TestFloatBatchModel(localcharacteristic)

    var selectionChecksum = 0L

    val combinedNanoseconds = measureNanoTime {
        repeat(benchmarkIterations) {
            characteristic.randomize(random)

            val selectedIndex =
                with(characteristic) {
                    with(random) {
                        choiceModel.select()
                    }
                }

            selectionChecksum += selectedIndex.toLong()
        }
    }

    val combinedTotalMs =
        combinedNanoseconds / 1_000_000.0

    val wallTimePerChoiceMs =
        combinedTotalMs / benchmarkIterations

    val choicesPerSecond =
        benchmarkIterations /
                (combinedTotalMs / 1_000.0)

    println("Threads: 1")
    println("Total wall time: $combinedTotalMs ms")
    println("Wall time per choice: $wallTimePerChoiceMs ms")
    println("Choices per second: $choicesPerSecond")
    println("Checksum: $selectionChecksum")
}

class Impedance(
    val travelTime: FloatArray,
    val cost: FloatArray,
    val distance: FloatArray,
    val travelTimePed: FloatArray,
    val travelTimeBike: FloatArray,
    val travelTimePut: FloatArray,
    val attractivities: FloatArray,
    val travelTimeMfFix: FloatArray,
    val travelTimeMf: FloatArray,
    val travelTimeFixpkw: FloatArray,
    val travelTimepkw: FloatArray,
    val travelTimeFixfuss: FloatArray,
    val travelTimefuss: FloatArray,
    val travelTimeFixoev: FloatArray,
    val travelTimeoev: FloatArray,
    val travelTimeFixrad: FloatArray,
    val travelTimerad: FloatArray,
    val travelCostFixpkw: FloatArray,
    val travelCostpkw: FloatArray,
    val travelCostFixoev: FloatArray,
    val travelCostoev: FloatArray,
    val zuabCarFix: FloatArray,
    val zuabCar: FloatArray,
    val zuabOevFix: FloatArray,
    val zuabOev: FloatArray,
) {
    companion object {
        fun random(size: Int): Impedance {

            val impedance = RandomSpawner(size).run {
                Impedance(
                    cost = randomFloatArray(),
                    distance = randomFloatArray(),
                    travelTimePed = randomFloatArray(),
                    travelTimeBike = randomFloatArray(),
                    travelTimePut = randomFloatArray(),
                    attractivities = randomFloatArray(50.0f, 100000.0f),
                    travelTimeMfFix = randomFloatArray(),
                    travelTimeMf = randomFloatArray(),
                    travelTimeFixpkw = randomFloatArray(),
                    travelTimepkw = randomFloatArray(),
                    travelTimeFixfuss = randomFloatArray(),
                    travelTimefuss = randomFloatArray(),
                    travelTimeFixoev = randomFloatArray(),
                    travelTimeoev = randomFloatArray(),
                    travelTimeFixrad = randomFloatArray(),
                    travelTimerad = randomFloatArray(),
                    travelCostFixpkw = randomFloatArray(),
                    travelCostpkw = randomFloatArray(),
                    travelCostFixoev = randomFloatArray(.0f, 10.0f),
                    travelCostoev = randomFloatArray(.0f, 10.0f),
                    zuabCarFix = randomFloatArray(),
                    zuabCar = randomFloatArray(),
                    zuabOevFix = randomFloatArray(),
                    zuabOev = randomFloatArray(),
                    travelTime = randomFloatArray(),
                )
            }
            return impedance
        }
    }
}

class RandomSpawner(private val arraySize: Int) {
    private val random = Random(42)

    fun randomFloatArray(start: Float = .0f, end: Float = 30.0f): FloatArray {
        val from: Double = start.toDouble()
        val until: Double = end.toDouble()
        return FloatArray(arraySize) { random.nextDouble(from, until).toFloat() }
    }

    fun randomInt(start: Int, end: Int): Int {
        return random.nextInt(start, end)
    }

    fun randomFloat(start: Float, end: Float): Float {
        return random.nextDouble(start.toDouble(), end.toDouble()).toFloat()
    }

}


class Attributes(
    var ageCode: Int,
    var employmentCode: Int,
    var hasCommuterTicket: Float,
    var hasGuaranteedCar: Float,
    var hasHighIncome: Float,
    var isUmland: Float,
    var activityTypeCode: Int,
    var distanceCode: Int,
    var availabilities: FloatArray,
) {
    
    fun randomize(random: Random) {
        ageCode = random.nextInt(0, 6)
        employmentCode = random.nextInt(0, 2)
        hasCommuterTicket = random.nextBooleanAsFloat()
        hasGuaranteedCar = random.nextBooleanAsFloat()
        hasHighIncome = random.nextBooleanAsFloat()
        isUmland = random.nextBooleanAsFloat()
        activityTypeCode = random.nextInt(0, 5)
        distanceCode = random.nextInt(0, 3)
        availabilities.forEachIndexed { index, value ->
            availabilities[index] = if(index == 3 || index == 0) 1f
                                    else Companion.random.nextBooleanAsFloat()
        }
    }
    companion object {
        private val random: Random = Random(43)
        fun random(): Attributes {
            return Attributes(
                random.nextInt(0, 6),
                random.nextInt(0, 2),
                random.nextBooleanAsFloat(),
                random.nextBooleanAsFloat(),
                random.nextBooleanAsFloat(),
                random.nextBooleanAsFloat(),
                random.nextInt(0, 5),
                random.nextInt(0, 3),
                FloatArray(5) { index ->
                    if(index == 3 || index == 0) 1f
                    else random.nextBooleanAsFloat()
                }
            )
        }
    }
}

fun Random.nextBooleanAsFloat(): Float {
    return if (nextBoolean()) 1.0f else 0.0f
}

object MyParameters {


    val asc_fuss: Float = (3.76685346228208 - 1).toFloat()
    val asc_rad: Float = (1.86423253977476).toFloat()
    val asc_bs: Float = (-2.74748353864304).toFloat()
    val asc_pkw: Float = (1.22529200766728).toFloat()
    val asc_mf: Float = (-0.280608946005487).toFloat()
    val asc_oev: Float = (0 + 1).toFloat()
    val asc_moia: Float = (-0.37705709382932).toFloat()
    val asc_cs_ff: Float = (-2.46414662819837).toFloat()
    val asc_cs_sb: Float = (-3.85787734173363).toFloat()
    val asc_escooter: Float = (-2.61071425609589).toFloat()
    val asc_taxi: Float = (-4.21831953675007).toFloat()
    val b_tt_fuss: Float = (-0.143588796056184 + 0.03).toFloat()
    val b_tt_rad: Float = (-0.152026975881995).toFloat()
    val b_tt_pkw: Float = (-0.0382017895663777 - 0.02).toFloat()
    val b_tt_mf_taxi: Float = (-0.0528979328553255).toFloat()
    val b_tt_oev: Float = (-0.0479879518424728).toFloat()
    val b_tt_bs: Float = (-0.127727726463221).toFloat()
    val b_tt_escooter: Float = (-0.124879896367696).toFloat()
    val b_tt_moia: Float = (-0.0739562799984561).toFloat()
    val b_zuab_pkw: Float = (-0.07635381474178).toFloat()
    val b_zuab_oev: Float = (-0.0264056325589302).toFloat()
    val b_zuab_cs_ff: Float = (-0.0998733766734251).toFloat()
    val b_zu_bs: Float = (-0.071827380024202).toFloat()
    val b_zu_es: Float = (-0.319085328690639).toFloat()
    val b_zuab_moia: Float = (-0.202978247661008).toFloat()
    val b_wt_moia: Float = (-0.144917240562579).toFloat()
    val b_cost_pkw: Float = (-0.116630151395857).toFloat()
    val b_cost_oev: Float = (-0.667070030829148).toFloat()
    val b_cost_taxi: Float = (-0.108195743922512).toFloat()
    val b_cost_bs_escooter: Float = (-0.130093425537354).toFloat()
    val b_cost_cs: Float = (-0.163102933626618).toFloat()
    val b_cost_moia: Float = (-0.105365775670613).toFloat()
    val b_nutzer_moia: Float = (1.04304109349849).toFloat()
    val b_arb_on_fuss: Float = (-0.611295790763783).toFloat()
    val b_dienst_on_fuss: Float = (-2.08288702898297).toFloat()
    val b_service_on_fuss: Float = (-0.690263124475946).toFloat()
    val b_freizeit_on_fuss: Float = (0.267490527971426).toFloat()
    val b_home_on_fuss: Float = (1.55831009663069).toFloat()
    val b_arb_on_rad: Float = (0.866143069057071).toFloat()
    val b_dienst_on_rad: Float = (-0.918456612362237).toFloat()
    val b_service_on_rad: Float = (-1.19958587723503).toFloat()
    val b_freizeit_on_rad: Float = (0).toFloat()
    val b_home_on_rad: Float = (1.29825292653342).toFloat()
    val b_arb_on_mf: Float = (-1.02713467302909).toFloat()
    val b_dienst_on_mf: Float = (-1.71859251059072).toFloat()
    val b_service_on_mf: Float = (-0.452457466806963).toFloat()
    val b_home_on_mf: Float = (1.34227424091334).toFloat()
    val b_arb_on_pkw: Float = (0).toFloat()
    val b_dienst_on_pkw: Float = (-1.28284371126842).toFloat()
    val b_service_on_pkw: Float = (-0.0891560297531824).toFloat()
    val b_freizeit_on_pkw: Float = (-1.00158195290812).toFloat()
    val b_home_on_pkw: Float = (1.00741039269339).toFloat()
    val b_arb_on_oev: Float = (0.814627986341692).toFloat()
    val b_dienst_on_oev: Float = (-0.41496303954142).toFloat()
    val b_service_on_oev: Float = (-0.103220410687913).toFloat()
    val b_freizeit_on_oev: Float = (0.0999365596556629).toFloat()
    val b_home_on_oev: Float = (1.50609681106275).toFloat()
    val b_arb_on_bs: Float = (0.955115013067832).toFloat()
    val b_dienst_on_bs: Float = (-0.726993715307142).toFloat()
    val b_freizeit_on_bs: Float = (0.183085627372588).toFloat()
    val b_arb_on_taxi: Float = (0.595912615053922).toFloat()
    val b_dienst_on_taxi: Float = (3.20705739471168).toFloat()
    val b_service_on_taxi: Float = (1.84303354885933).toFloat()
    val b_freizeit_on_taxi: Float = (1.15768641383172).toFloat()
    val b_home_on_taxi: Float = (2.90369389142055).toFloat()
    val b_arb_on_cs_ff: Float = (-0.206950523845975).toFloat()
    val b_dienst_on_cs_ff: Float = (-1.03601319882931).toFloat()
    val b_service_on_cs_ff: Float = (-0.161620475279169).toFloat()
    val b_freizeit_on_cs_ff: Float = (-0.653204676851787).toFloat()
    val b_arb_on_escooter: Float = (1.01755198945501).toFloat()
    val b_dienst_on_escooter: Float = (-0.0862734391017355).toFloat()
    val b_service_on_escooter: Float = (0).toFloat()
    val b_freizeit_on_escooter: Float = (0.506305350701758).toFloat()
    val b_park_oev: Float = (0.426245178308876).toFloat()
    val elasticity_park_oev: Float = (0.597562490122148).toFloat()
    val b_logsum_pt_active: Float = (0.37913097643898 - 0.1).toFloat()
    val b_logsum_drive: Float = (0.101571780926651 - 0.05).toFloat()
    val b_logsum_pt_active_fix: Float = (0.0223919993684166).toFloat()
    val b_logsum_drive_fix: Float = (0.321408622664327).toFloat()
    val b_attr: Float = (0.164790334596064 - 0.1).toFloat()
    val b_parken: Float = (-0.000805225283993312).toFloat()
    val elasticity_parken: Float = (2.03231417905929).toFloat()
    val b_0_1: Float = (4.01726106476279).toFloat()
    val b_1_2: Float = (-1.2807770160566 + 0.4).toFloat()
    val shift_b_0_1_on_attr: Float = (-0.391440604191497).toFloat()
    val shift_b_1_2_on_attr: Float = (0.165687668821311).toFloat()
    val shift_age_2_on_logsum_pt_active: Float = (0).toFloat()
    val shift_age_2_on_logsum_pt_active_fix: Float = (0).toFloat()
    val shift_age_2_on_logsum_drive: Float = (0).toFloat()
    val shift_age_2_on_logsum_drive_fix: Float = (0).toFloat()
    val shift_age_2_on_attr: Float = (0).toFloat()
    val shift_age_3_on_logsum_pt_active: Float = (0).toFloat()
    val shift_age_3_on_logsum_pt_active_fix: Float = (0).toFloat()
    val shift_age_3_on_logsum_drive: Float = (0).toFloat()
    val shift_age_3_on_logsum_drive_fix: Float = (0).toFloat()
    val shift_age_3_on_attr: Float = (0).toFloat()
    val shift_age_4_on_logsum_drive: Float = (0).toFloat()
    val shift_age_4_on_logsum_drive_fix: Float = (0).toFloat()
    val shift_age_4_on_logsum_pt_active: Float = (0).toFloat()
    val shift_age_4_on_logsum_pt_active_fix: Float = (0).toFloat()
    val shift_age_4_on_attr: Float = (0).toFloat()
    val shift_age_56_on_logsum_pt_active: Float = (0).toFloat()
    val shift_age_56_on_logsum_pt_active_fix: Float = (0).toFloat()
    val shift_age_56_on_logsum_drive: Float = (0).toFloat()
    val shift_age_56_on_logsum_drive_fix: Float = (0).toFloat()
    val shift_age_56_on_attr: Float = (0).toFloat()
    val shift_age_78_on_logsum_pt_active: Float = (0).toFloat()
    val shift_age_78_on_logsum_pt_active_fix: Float = (0).toFloat()
    val shift_age_78_on_logsum_drive: Float = (0).toFloat()
    val shift_age_78_on_logsum_drive_fix: Float = (0).toFloat()
    val shift_age_78_on_attr: Float = (0 + 0.1).toFloat()
    val shift_arb_on_logsum_pt_active: Float = (0).toFloat()
    val shift_arb_on_logsum_pt_active_fix: Float = (0).toFloat()
    val shift_arb_on_logsum_drive: Float = (0).toFloat()
    val shift_arb_on_logsum_drive_fix: Float = (0).toFloat()
    val shift_arb_on_attr: Float = (0 + 0.1).toFloat()
    val shift_educ_on_logsum_pt_active: Float = (0).toFloat()
    val shift_educ_on_logsum_pt_active_fix: Float = (0).toFloat()
    val shift_educ_on_logsum_drive: Float = (0).toFloat()
    val shift_educ_on_logsum_drive_fix: Float = (0).toFloat()
    val shift_educ_on_attr: Float = (0 - 0.05).toFloat()
    val shift_high_inc_on_logsum_pt_active: Float = (0).toFloat()
    val shift_high_inc_on_logsum_pt_active_fix: Float = (0).toFloat()
    val shift_high_inc_on_logsum_drive: Float = (0).toFloat()
    val shift_high_inc_on_logsum_drive_fix: Float = (0).toFloat()
    val shift_high_inc_on_attr: Float = (0).toFloat()
    val shift_zk_on_logsum_pt_active: Float = (0).toFloat()
    val shift_zk_on_logsum_pt_active_fix: Float = (0).toFloat()
    val shift_zk_on_logsum_drive: Float = (0).toFloat()
    val shift_zk_on_logsum_drive_fix: Float = (0).toFloat()
    val shift_zk_on_attr: Float = (0).toFloat()
    val shift_carav_on_logsum_pt_active: Float = (0).toFloat()
    val shift_carav_on_logsum_pt_active_fix: Float = (0).toFloat()
    val shift_carav_on_logsum_drive: Float = (0).toFloat()
    val shift_carav_on_logsum_drive_fix: Float = (0).toFloat()
    val shift_carav_on_attr: Float = (0.1).toFloat()
    val shift_uml_on_logsum_pt_active: Float = (0.0640383675977011 + 0.1).toFloat()
    val shift_uml_on_logsum_pt_active_fix: Float = (0.0416507340946399 + 0.1).toFloat()
    val shift_uml_on_logsum_drive: Float = (0.0462160663655077 + 0.1).toFloat()
    val shift_uml_on_logsum_drive_fix: Float = (-0.0755006569431623 + 0.1).toFloat()
    val shift_uml_on_attr: Float = (0.610291409419243 - 0.2).toFloat()
    val max_attractivity: Float = (20000).toFloat()

    val actTypesBikeFix = floatArrayOf(b_arb_on_rad, b_dienst_on_rad, b_freizeit_on_rad, b_service_on_rad, 0.0f, 0.0f)
    val actTypesCarFix = floatArrayOf(b_arb_on_pkw, b_dienst_on_pkw, 0.0f, b_service_on_pkw, b_home_on_pkw, 0.0f)
    val actTypesMfFix = floatArrayOf(b_arb_on_mf, b_dienst_on_mf, 0.0f, b_service_on_mf, b_home_on_mf, 0.0f)
    val actTypesPTFix = floatArrayOf(b_arb_on_oev, b_dienst_on_oev, b_freizeit_on_oev, b_service_on_oev, 0.0f, 0.0f)
    val actTypesPedFix =
        floatArrayOf(b_arb_on_fuss, b_dienst_on_fuss, b_freizeit_on_fuss, b_service_on_fuss, 0.0f, 0.0f)
    val distanceEffectsGlobal = floatArrayOf(0.0f, b_0_1, b_1_2, 0.0f)
    val ageEffects = floatArrayOf(
        0.0f,
        shift_age_2_on_attr,
        shift_age_3_on_attr,
        shift_age_4_on_attr,
        shift_age_56_on_attr,
        shift_age_78_on_attr,
        0.0f,
    )
    val employmentEffects = floatArrayOf(shift_educ_on_attr, shift_arb_on_attr, 0.0f)
    val distanceEffects = floatArrayOf(0.0f, shift_b_0_1_on_attr, shift_b_1_2_on_attr, 0.0f)
    val ageEffectsPt = floatArrayOf(
        0.0f,
        shift_age_2_on_logsum_pt_active,
        shift_age_3_on_logsum_pt_active,
        shift_age_4_on_logsum_pt_active,
        shift_age_56_on_logsum_pt_active,
        shift_age_78_on_logsum_pt_active,
        0.0f,
    )
    val employmentEffectsPt = floatArrayOf(shift_educ_on_logsum_pt_active, shift_arb_on_logsum_pt_active, 0.0f)
    val ageEffectsPtFix = floatArrayOf(
        0.0f,
        shift_age_2_on_logsum_pt_active_fix,
        shift_age_3_on_logsum_pt_active_fix,
        shift_age_4_on_logsum_pt_active_fix,
        shift_age_56_on_logsum_pt_active_fix,
        shift_age_78_on_logsum_pt_active_fix,
        0.0f,
    )
    val employmentEffectsPtFix =
        floatArrayOf(shift_educ_on_logsum_pt_active_fix, shift_arb_on_logsum_pt_active_fix, 0.0f)
    val ageEffectsDriveFix = floatArrayOf(
        0.0f,
        shift_age_2_on_logsum_drive_fix,
        shift_age_3_on_logsum_drive_fix,
        shift_age_4_on_logsum_drive_fix,
        shift_age_56_on_logsum_drive_fix,
        shift_age_78_on_logsum_drive_fix,
        0.0f,
    )
    val employmentEffectsDriveFix = floatArrayOf(shift_educ_on_logsum_drive_fix, shift_arb_on_logsum_drive_fix, 0.0f)
    val ageEffectsDrive = floatArrayOf(
        0.0f,
        shift_age_2_on_logsum_drive,
        shift_age_3_on_logsum_drive,
        shift_age_4_on_logsum_drive,
        shift_age_56_on_logsum_drive,
        shift_age_78_on_logsum_drive,
        0.0f,
    )
    val employmentEffectsDrive = floatArrayOf(shift_educ_on_logsum_drive, shift_arb_on_logsum_drive, 0.0f)
}

internal enum class Mode {
    CAR,
    BIKE,
    PT,
    PED,
    PASSENGER,
};

class TestFloatBatchModel(private val localcharacteristic: Impedance,
                          override val parameters: MyParameters = MyParameters,
                          override val distributionFunction: FloatCumulateDistributionArray<Any?> = FloatMultinomialLogitArray(),
                          override val selectionFunction: FloatSelectionFunctionArray = FloatWeightedBinarySelection(),
) : FloatBatchUtilityChoiceModel<Attributes, MyParameters> {

    override val size = localcharacteristic.travelTime.size


    val result = FloatArray(size)

    context(characteristic: Attributes)
    override fun MyParameters.generateUtilitiesArray(): FloatArray {

        val attrBase =
            (b_attr + ageEffects[characteristic.ageCode] + employmentEffects[characteristic.employmentCode] + shift_zk_on_attr * characteristic.hasCommuterTicket + shift_carav_on_attr * characteristic.hasGuaranteedCar + shift_high_inc_on_attr * characteristic.hasHighIncome + shift_uml_on_attr * characteristic.isUmland + distanceEffects[characteristic.distanceCode])
        val oevBase =
            (b_logsum_pt_active + ageEffectsPt[characteristic.ageCode] + employmentEffectsPt[characteristic.employmentCode] + shift_zk_on_logsum_pt_active * characteristic.hasCommuterTicket + shift_carav_on_logsum_pt_active * characteristic.hasGuaranteedCar + shift_high_inc_on_logsum_pt_active * characteristic.hasHighIncome + shift_uml_on_logsum_pt_active * characteristic.isUmland)
        val baseDrive =
            (b_logsum_drive + ageEffectsDrive[characteristic.ageCode] + employmentEffectsDrive[characteristic.employmentCode] + shift_zk_on_logsum_drive * characteristic.hasCommuterTicket + shift_carav_on_logsum_drive * characteristic.hasGuaranteedCar + shift_high_inc_on_logsum_drive * characteristic.hasHighIncome + shift_uml_on_logsum_drive * characteristic.isUmland)
        val oevFix =
            (b_logsum_pt_active_fix + ageEffectsPtFix[characteristic.ageCode] + employmentEffectsPtFix[characteristic.employmentCode] + shift_zk_on_logsum_pt_active_fix * characteristic.hasCommuterTicket + shift_carav_on_logsum_pt_active_fix * characteristic.hasGuaranteedCar + shift_high_inc_on_logsum_pt_active_fix * characteristic.hasHighIncome + shift_uml_on_logsum_pt_active_fix * characteristic.isUmland)
        val baseDriveFix =
            (b_logsum_drive_fix + ageEffectsDriveFix[characteristic.ageCode] + employmentEffectsDriveFix[characteristic.employmentCode] + shift_zk_on_logsum_drive_fix * characteristic.hasCommuterTicket + shift_carav_on_logsum_drive_fix * characteristic.hasGuaranteedCar + shift_high_inc_on_logsum_drive_fix * characteristic.hasHighIncome + shift_uml_on_logsum_drive_fix * characteristic.isUmland)
        for (i in 0 until size) {
            val attractivityLogged = ln(min(localcharacteristic.attractivities[i], max_attractivity))
            val oevLogsum = ln(
                characteristic.availabilities[Mode.PT.ordinal] * exp(
                    asc_oev + b_tt_oev * min(
                        localcharacteristic.travelTimeoev[i],
                        999.0f,
                    ) + b_cost_oev * min(
                        localcharacteristic.travelCostoev[i],
                        999.0f,
                    ) + b_zuab_oev * min(
                        localcharacteristic.zuabOev[i],
                        999.0f,
                    ) + actTypesPTFix[characteristic.activityTypeCode],
                ) + characteristic.availabilities[Mode.PED.ordinal] * exp(
                    asc_fuss + b_tt_fuss * min(
                        localcharacteristic.travelTimefuss[i],
                        999.0f,
                    ) + actTypesPedFix[characteristic.activityTypeCode],
                ) + characteristic.availabilities[Mode.BIKE.ordinal] * exp(
                    asc_rad + b_tt_rad * min(
                        localcharacteristic.travelTimerad[i],
                        999.0f,
                    ) + actTypesBikeFix[characteristic.activityTypeCode],
                ),
            )
            val logsumDrive = ln(
                characteristic.availabilities[Mode.CAR.ordinal] * exp(
                    asc_pkw + b_tt_pkw * min(
                        localcharacteristic.travelTimepkw[i],
                        999.0f,
                    ) + b_cost_pkw * min(
                        localcharacteristic.travelCostpkw[i],
                        999.0f,
                    ) + b_zuab_pkw * min(
                        localcharacteristic.zuabCar[i],
                        999.0f,
                    ) + actTypesCarFix[characteristic.activityTypeCode],
                ) + characteristic.availabilities[Mode.PASSENGER.ordinal] * exp(
                    asc_mf + b_tt_mf_taxi * min(
                        localcharacteristic.travelTimeMf[i],
                        999.0f,
                    ) + actTypesMfFix[characteristic.activityTypeCode],
                ),
            )
            val uOev = (asc_oev + b_tt_oev * min(localcharacteristic.travelTimeFixoev[i], 999.0f) + b_cost_oev * min(
                localcharacteristic.travelCostFixoev[i],
                999.0f,
            ) + b_zuab_oev * min(localcharacteristic.zuabOevFix[i], 999.0f) + actTypesPTFix[characteristic.activityTypeCode])
            val oevExp = exp(uOev)
            val uPed = (asc_fuss + b_tt_fuss * min(
                localcharacteristic.travelTimeFixfuss[i],
                999.0f,
            ) + actTypesPedFix[characteristic.activityTypeCode])
            val pedExp = exp(uPed)
            val bikeExp = exp(
                asc_rad + b_tt_rad * min(
                    localcharacteristic.travelTimeFixrad[i],
                    999.0f,
                ) + actTypesBikeFix[characteristic.activityTypeCode],
            )
            val logsumOevFix =
                ln(characteristic.availabilities[Mode.PT.ordinal] * oevExp + characteristic.availabilities[Mode.PED.ordinal] * pedExp + characteristic.availabilities[Mode.BIKE.ordinal] * bikeExp)
            val logsumDriveFix = ln(
                characteristic.availabilities[Mode.CAR.ordinal] * exp(
                    asc_pkw + b_tt_pkw * min(
                        localcharacteristic.travelTimeFixpkw[i],
                        999.0f,
                    ) + b_cost_pkw * min(localcharacteristic.travelCostFixpkw[i], 999.0f) + b_zuab_pkw * min(
                        localcharacteristic.zuabCarFix[i],
                        999.0f,
                    ) + actTypesCarFix[characteristic.activityTypeCode],
                ) + characteristic.availabilities[Mode.PASSENGER.ordinal] * exp(
                    asc_mf + b_tt_mf_taxi * min(
                        localcharacteristic.travelTimeMfFix[i],
                        999.0f,
                    ) + actTypesMfFix[characteristic.activityTypeCode],
                ),
            )
            result[i] =
                (attrBase * attractivityLogged + distanceEffectsGlobal[characteristic.distanceCode] + oevBase * oevLogsum + baseDrive * logsumDrive + oevFix * logsumOevFix + baseDriveFix * logsumDriveFix)
        }
        return result
    }
}

