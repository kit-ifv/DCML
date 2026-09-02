package benchmark

import edu.kit.ifv.mobitopp.discretechoice.distribution.CumulateDistributionArray
import edu.kit.ifv.mobitopp.discretechoice.distribution.MultinomialLogitArray
import edu.kit.ifv.mobitopp.discretechoice.models.BatchUtilityChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.selection.SelectionFunctionArray
import edu.kit.ifv.mobitopp.discretechoice.selection.WeightedSelection
import kotlin.concurrent.thread
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.system.measureNanoTime

fun main() {
    val alternativeCount = 96
    val benchmarkIterations = 1_000_000
    val seed = 42
    val threadCount = max(1, Runtime.getRuntime().availableProcessors())
    val localAttributes: ImpedanceDouble =
        ImpedanceDouble.random(size = alternativeCount)
    val results = Array(threadCount) { ThreadResultD() }
    val threads = ArrayList<Thread>(threadCount)
    val combinedNanoseconds = measureNanoTime {
        for (threadIndex in 0 until threadCount) {
            threads += thread(start = true) {
                val random = Random(seed + threadIndex)
                val attributes = AttributesDouble.random() // One model per thread avoids accidental shared mutable state.
                val choiceModel = TestDoubleBatchChoiceModel(localAttributes)
                val begin = benchmarkIterations.toLong() * threadIndex / threadCount
                val end = benchmarkIterations.toLong() * (threadIndex + 1) / threadCount
                val threadResult = results[threadIndex]
                for (iteration in begin until end) {
                    attributes.randomize(random)
                    val selectedIndex =
                        with(attributes) { with(random) { choiceModel.select() } }
                    threadResult.checksum += selectedIndex.toLong()
                }
            }
        }
        for (worker in threads) {
            worker.join()
        }
    }
    var selectionChecksum = 0L
    for (result in results) {
        selectionChecksum += result.checksum
    }
    val combinedTotalMs = combinedNanoseconds / 1_000_000.0
    val wallTimePerChoiceMs = combinedTotalMs / benchmarkIterations
    val choicesPerSecond =
        benchmarkIterations / (combinedTotalMs / 1_000.0)
    println("Threads: $threadCount")
    println("Total wall time: $combinedTotalMs ms")
    println("Wall time per choice: $wallTimePerChoiceMs ms")
    println("Choices per second: $choicesPerSecond")
    println("Checksum: $selectionChecksum")
}

class TestDoubleBatchChoiceModel(
    private val localAttributes: ImpedanceDouble,
    parameters: MyParametersDouble = MyParametersDouble,
    override val name: String = "testModel"
) : BatchUtilityChoiceModel<AttributesDouble, MyParametersDouble, Int>(
    parameters = parameters,
    choices = IntArray(localAttributes.travelTime.size){it}.toSet()
) {
    val size: Int = localAttributes.travelTime.size

    context(characteristic: AttributesDouble)
    override fun MyParametersDouble.generateUtilitiesArray(): DoubleArray {
        val result = DoubleArray(size)

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
            val attractivityLogged = ln(min(localAttributes.attractivities[i], max_attractivity))
            val oevLogsum = ln(
                characteristic.availabilities[Mode.PT.ordinal] * exp(
                    asc_oev + b_tt_oev * min(
                        localAttributes.travelTimeoev[i],
                        999.0,
                    ) + b_cost_oev * min(
                        localAttributes.travelCostoev[i],
                        999.0,
                    ) + b_zuab_oev * min(
                        localAttributes.zuabOev[i],
                        999.0,
                    ) + actTypesPTFix[characteristic.activityTypeCode],
                ) + characteristic.availabilities[Mode.PED.ordinal] * exp(
                    asc_fuss + b_tt_fuss * min(
                        localAttributes.travelTimefuss[i],
                        999.0,
                    ) + actTypesPedFix[characteristic.activityTypeCode],
                ) + characteristic.availabilities[Mode.BIKE.ordinal] * exp(
                    asc_rad + b_tt_rad * min(
                        localAttributes.travelTimerad[i],
                        999.0,
                    ) + actTypesBikeFix[characteristic.activityTypeCode],
                ),
            )
            val logsumDrive = ln(
                characteristic.availabilities[Mode.CAR.ordinal] * exp(
                    asc_pkw + b_tt_pkw * min(
                        localAttributes.travelTimepkw[i],
                        999.0,
                    ) + b_cost_pkw * min(
                        localAttributes.travelCostpkw[i],
                        999.0,
                    ) + b_zuab_pkw * min(
                        localAttributes.zuabCar[i],
                        999.0,
                    ) + actTypesCarFix[characteristic.activityTypeCode],
                ) + characteristic.availabilities[Mode.PASSENGER.ordinal] * exp(
                    asc_mf + b_tt_mf_taxi * min(
                        localAttributes.travelTimeMf[i],
                        999.0,
                    ) + actTypesMfFix[characteristic.activityTypeCode],
                ),
            )
            val uOev = (asc_oev + b_tt_oev * min(localAttributes.travelTimeFixoev[i], 999.0) + b_cost_oev * min(
                localAttributes.travelCostFixoev[i],
                999.0,
            ) + b_zuab_oev * min(localAttributes.zuabOevFix[i], 999.0) + actTypesPTFix[characteristic.activityTypeCode])
            val oevExp = exp(uOev)
            val uPed = (asc_fuss + b_tt_fuss * min(
                localAttributes.travelTimeFixfuss[i],
                999.0,
            ) + actTypesPedFix[characteristic.activityTypeCode])
            val pedExp = exp(uPed)
            val bikeExp = exp(
                asc_rad + b_tt_rad * min(
                    localAttributes.travelTimeFixrad[i],
                    999.0,
                ) + actTypesBikeFix[characteristic.activityTypeCode],
            )
            val logsumOevFix =
                ln(characteristic.availabilities[Mode.PT.ordinal] * oevExp +
                        characteristic.availabilities[Mode.PED.ordinal] * pedExp +
                        characteristic.availabilities[Mode.BIKE.ordinal] * bikeExp)
            val logsumDriveFix = ln(
                characteristic.availabilities[Mode.CAR.ordinal] * exp(
                    asc_pkw + b_tt_pkw * min(
                        localAttributes.travelTimeFixpkw[i],
                        999.0,
                    ) + b_cost_pkw * min(localAttributes.travelCostFixpkw[i], 999.0) + b_zuab_pkw * min(
                        localAttributes.zuabCarFix[i],
                        999.0,
                    ) + actTypesCarFix[characteristic.activityTypeCode],
                ) + characteristic.availabilities[Mode.PASSENGER.ordinal] * exp(
                    asc_mf + b_tt_mf_taxi * min(
                        localAttributes.travelTimeMfFix[i],
                        999.0,
                    ) + actTypesMfFix[characteristic.activityTypeCode],
                ),
            )
            result[i] =
                (attrBase * attractivityLogged + distanceEffectsGlobal[characteristic.distanceCode] +
                        oevBase * oevLogsum +
                        baseDrive * logsumDrive +
                        oevFix * logsumOevFix +
                        baseDriveFix * logsumDriveFix)
        }
        return result
    }
}

private data class ThreadResultD(var checksum: Long = 0L)


class ImpedanceDouble(
    val travelTime: DoubleArray,
    val cost: DoubleArray,
    val distance: DoubleArray,
    val travelTimePed: DoubleArray,
    val travelTimeBike: DoubleArray,
    val travelTimePut: DoubleArray,
    val attractivities: DoubleArray,
    val travelTimeMfFix: DoubleArray,
    val travelTimeMf: DoubleArray,
    val travelTimeFixpkw: DoubleArray,
    val travelTimepkw: DoubleArray,
    val travelTimeFixfuss: DoubleArray,
    val travelTimefuss: DoubleArray,
    val travelTimeFixoev: DoubleArray,
    val travelTimeoev: DoubleArray,
    val travelTimeFixrad: DoubleArray,
    val travelTimerad: DoubleArray,
    val travelCostFixpkw: DoubleArray,
    val travelCostpkw: DoubleArray,
    val travelCostFixoev: DoubleArray,
    val travelCostoev: DoubleArray,
    val zuabCarFix: DoubleArray,
    val zuabCar: DoubleArray,
    val zuabOevFix: DoubleArray,
    val zuabOev: DoubleArray,
) {
    companion object {
        fun random(size: Int): ImpedanceDouble {

            val impedance = RandomSpawnerDouble(size).run {
                ImpedanceDouble(
                    cost = randomDoubleArray(),
                    distance = randomDoubleArray(),
                    travelTimePed = randomDoubleArray(),
                    travelTimeBike = randomDoubleArray(),
                    travelTimePut = randomDoubleArray(),
                    attractivities = randomDoubleArray(50.0, 100000.0),
                    travelTimeMfFix = randomDoubleArray(),
                    travelTimeMf = randomDoubleArray(),
                    travelTimeFixpkw = randomDoubleArray(),
                    travelTimepkw = randomDoubleArray(),
                    travelTimeFixfuss = randomDoubleArray(),
                    travelTimefuss = randomDoubleArray(),
                    travelTimeFixoev = randomDoubleArray(),
                    travelTimeoev = randomDoubleArray(),
                    travelTimeFixrad = randomDoubleArray(),
                    travelTimerad = randomDoubleArray(),
                    travelCostFixpkw = randomDoubleArray(),
                    travelCostpkw = randomDoubleArray(),
                    travelCostFixoev = randomDoubleArray(.0, 10.0),
                    travelCostoev = randomDoubleArray(.0, 10.0),
                    zuabCarFix = randomDoubleArray(),
                    zuabCar = randomDoubleArray(),
                    zuabOevFix = randomDoubleArray(),
                    zuabOev = randomDoubleArray(),
                    travelTime = randomDoubleArray(),
                )
            }
            return impedance
        }
    }
}

class RandomSpawnerDouble(private val arraySize: Int) {
    private val random = Random(42)

    fun randomDoubleArray(start: Double = .0, end: Double = 30.0): DoubleArray {
        val from: Double = start.toDouble()
        val until: Double = end.toDouble()
        return DoubleArray(arraySize) { random.nextDouble(from, until).toDouble() }
    }

    fun randomInt(start: Int, end: Int): Int {
        return random.nextInt(start, end)
    }

    fun randomDouble(start: Double, end: Double): Double {
        return random.nextDouble(start.toDouble(), end.toDouble()).toDouble()
    }

}


class AttributesDouble(
    var ageCode: Int,
    var employmentCode: Int,
    var hasCommuterTicket: Double,
    var hasGuaranteedCar: Double,
    var hasHighIncome: Double,
    var isUmland: Double,
    var activityTypeCode: Int,
    var distanceCode: Int,
    var availabilities: DoubleArray,
) {

    fun randomize(random: Random) {
        ageCode = random.nextInt(0, 6)
        employmentCode = random.nextInt(0, 2)
        hasCommuterTicket = random.nextBooleanAsDouble()
        hasGuaranteedCar = random.nextBooleanAsDouble()
        hasHighIncome = random.nextBooleanAsDouble()
        isUmland = random.nextBooleanAsDouble()
        activityTypeCode = random.nextInt(0, 5)
        distanceCode = random.nextInt(0, 3)
        availabilities.forEachIndexed { index, value ->
            availabilities[index] = random.nextBooleanAsDouble()
            availabilities[0] = 1.0
            availabilities[3] = 1.0 // guarantee one mode is available and logsums are not -negative infinity
        }
    }
    companion object {
        private val random: Random = Random(43)
        fun random(): AttributesDouble {
            return AttributesDouble(
                random.nextInt(0, 6),
                random.nextInt(0, 2),
                random.nextBooleanAsDouble(),
                random.nextBooleanAsDouble(),
                random.nextBooleanAsDouble(),
                random.nextBooleanAsDouble(),
                random.nextInt(0, 5),
                random.nextInt(0, 3),
                DoubleArray(5) {
                    random.nextBooleanAsDouble()
                }.let {
                    DoubleArray(5) { index ->
                        if (index == 3 || index == 0) 1.0
                        else it[index]
                    }
                }
            )
        }
    }
}

fun Random.nextBooleanAsDouble(): Double {
    return if (nextBoolean()) 1.0 else 0.0
}

object MyParametersDouble {
    val asc_fuss: Double = (3.76685346228208 - 1).toDouble()
    val asc_rad: Double = (1.86423253977476).toDouble()
    val asc_bs: Double = (-2.74748353864304).toDouble()
    val asc_pkw: Double = (1.22529200766728).toDouble()
    val asc_mf: Double = (-0.280608946005487).toDouble()
    val asc_oev: Double = (0 + 1).toDouble()
    val asc_moia: Double = (-0.37705709382932).toDouble()
    val asc_cs_ff: Double = (-2.46414662819837).toDouble()
    val asc_cs_sb: Double = (-3.85787734173363).toDouble()
    val asc_escooter: Double = (-2.61071425609589).toDouble()
    val asc_taxi: Double = (-4.21831953675007).toDouble()
    val b_tt_fuss: Double = (-0.143588796056184 + 0.03).toDouble()
    val b_tt_rad: Double = (-0.152026975881995).toDouble()
    val b_tt_pkw: Double = (-0.0382017895663777 - 0.02).toDouble()
    val b_tt_mf_taxi: Double = (-0.0528979328553255).toDouble()
    val b_tt_oev: Double = (-0.0479879518424728).toDouble()
    val b_tt_bs: Double = (-0.127727726463221).toDouble()
    val b_tt_escooter: Double = (-0.124879896367696).toDouble()
    val b_tt_moia: Double = (-0.0739562799984561).toDouble()
    val b_zuab_pkw: Double = (-0.07635381474178).toDouble()
    val b_zuab_oev: Double = (-0.0264056325589302).toDouble()
    val b_zuab_cs_ff: Double = (-0.0998733766734251).toDouble()
    val b_zu_bs: Double = (-0.071827380024202).toDouble()
    val b_zu_es: Double = (-0.319085328690639).toDouble()
    val b_zuab_moia: Double = (-0.202978247661008).toDouble()
    val b_wt_moia: Double = (-0.144917240562579).toDouble()
    val b_cost_pkw: Double = (-0.116630151395857).toDouble()
    val b_cost_oev: Double = (-0.667070030829148).toDouble()
    val b_cost_taxi: Double = (-0.108195743922512).toDouble()
    val b_cost_bs_escooter: Double = (-0.130093425537354).toDouble()
    val b_cost_cs: Double = (-0.163102933626618).toDouble()
    val b_cost_moia: Double = (-0.105365775670613).toDouble()
    val b_nutzer_moia: Double = (1.04304109349849).toDouble()
    val b_arb_on_fuss: Double = (-0.611295790763783).toDouble()
    val b_dienst_on_fuss: Double = (-2.08288702898297).toDouble()
    val b_service_on_fuss: Double = (-0.690263124475946).toDouble()
    val b_freizeit_on_fuss: Double = (0.267490527971426).toDouble()
    val b_home_on_fuss: Double = (1.55831009663069).toDouble()
    val b_arb_on_rad: Double = (0.866143069057071).toDouble()
    val b_dienst_on_rad: Double = (-0.918456612362237).toDouble()
    val b_service_on_rad: Double = (-1.19958587723503).toDouble()
    val b_freizeit_on_rad: Double = (0).toDouble()
    val b_home_on_rad: Double = (1.29825292653342).toDouble()
    val b_arb_on_mf: Double = (-1.02713467302909).toDouble()
    val b_dienst_on_mf: Double = (-1.71859251059072).toDouble()
    val b_service_on_mf: Double = (-0.452457466806963).toDouble()
    val b_home_on_mf: Double = (1.34227424091334).toDouble()
    val b_arb_on_pkw: Double = (0).toDouble()
    val b_dienst_on_pkw: Double = (-1.28284371126842).toDouble()
    val b_service_on_pkw: Double = (-0.0891560297531824).toDouble()
    val b_freizeit_on_pkw: Double = (-1.00158195290812).toDouble()
    val b_home_on_pkw: Double = (1.00741039269339).toDouble()
    val b_arb_on_oev: Double = (0.814627986341692).toDouble()
    val b_dienst_on_oev: Double = (-0.41496303954142).toDouble()
    val b_service_on_oev: Double = (-0.103220410687913).toDouble()
    val b_freizeit_on_oev: Double = (0.0999365596556629).toDouble()
    val b_home_on_oev: Double = (1.50609681106275).toDouble()
    val b_arb_on_bs: Double = (0.955115013067832).toDouble()
    val b_dienst_on_bs: Double = (-0.726993715307142).toDouble()
    val b_freizeit_on_bs: Double = (0.183085627372588).toDouble()
    val b_arb_on_taxi: Double = (0.595912615053922).toDouble()
    val b_dienst_on_taxi: Double = (3.20705739471168).toDouble()
    val b_service_on_taxi: Double = (1.84303354885933).toDouble()
    val b_freizeit_on_taxi: Double = (1.15768641383172).toDouble()
    val b_home_on_taxi: Double = (2.90369389142055).toDouble()
    val b_arb_on_cs_ff: Double = (-0.206950523845975).toDouble()
    val b_dienst_on_cs_ff: Double = (-1.03601319882931).toDouble()
    val b_service_on_cs_ff: Double = (-0.161620475279169).toDouble()
    val b_freizeit_on_cs_ff: Double = (-0.653204676851787).toDouble()
    val b_arb_on_escooter: Double = (1.01755198945501).toDouble()
    val b_dienst_on_escooter: Double = (-0.0862734391017355).toDouble()
    val b_service_on_escooter: Double = (0).toDouble()
    val b_freizeit_on_escooter: Double = (0.506305350701758).toDouble()
    val b_park_oev: Double = (0.426245178308876).toDouble()
    val elasticity_park_oev: Double = (0.597562490122148).toDouble()
    val b_logsum_pt_active: Double = (0.37913097643898 - 0.1).toDouble()
    val b_logsum_drive: Double = (0.101571780926651 - 0.05).toDouble()
    val b_logsum_pt_active_fix: Double = (0.0223919993684166).toDouble()
    val b_logsum_drive_fix: Double = (0.321408622664327).toDouble()
    val b_attr: Double = (0.164790334596064 - 0.1).toDouble()
    val b_parken: Double = (-0.000805225283993312).toDouble()
    val elasticity_parken: Double = (2.03231417905929).toDouble()
    val b_0_1: Double = (4.01726106476279).toDouble()
    val b_1_2: Double = (-1.2807770160566 + 0.4).toDouble()
    val shift_b_0_1_on_attr: Double = (-0.391440604191497).toDouble()
    val shift_b_1_2_on_attr: Double = (0.165687668821311).toDouble()
    val shift_age_2_on_logsum_pt_active: Double = (0).toDouble()
    val shift_age_2_on_logsum_pt_active_fix: Double = (0).toDouble()
    val shift_age_2_on_logsum_drive: Double = (0).toDouble()
    val shift_age_2_on_logsum_drive_fix: Double = (0).toDouble()
    val shift_age_2_on_attr: Double = (0).toDouble()
    val shift_age_3_on_logsum_pt_active: Double = (0).toDouble()
    val shift_age_3_on_logsum_pt_active_fix: Double = (0).toDouble()
    val shift_age_3_on_logsum_drive: Double = (0).toDouble()
    val shift_age_3_on_logsum_drive_fix: Double = (0).toDouble()
    val shift_age_3_on_attr: Double = (0).toDouble()
    val shift_age_4_on_logsum_drive: Double = (0).toDouble()
    val shift_age_4_on_logsum_drive_fix: Double = (0).toDouble()
    val shift_age_4_on_logsum_pt_active: Double = (0).toDouble()
    val shift_age_4_on_logsum_pt_active_fix: Double = (0).toDouble()
    val shift_age_4_on_attr: Double = (0).toDouble()
    val shift_age_56_on_logsum_pt_active: Double = (0).toDouble()
    val shift_age_56_on_logsum_pt_active_fix: Double = (0).toDouble()
    val shift_age_56_on_logsum_drive: Double = (0).toDouble()
    val shift_age_56_on_logsum_drive_fix: Double = (0).toDouble()
    val shift_age_56_on_attr: Double = (0).toDouble()
    val shift_age_78_on_logsum_pt_active: Double = (0).toDouble()
    val shift_age_78_on_logsum_pt_active_fix: Double = (0).toDouble()
    val shift_age_78_on_logsum_drive: Double = (0).toDouble()
    val shift_age_78_on_logsum_drive_fix: Double = (0).toDouble()
    val shift_age_78_on_attr: Double = (0 + 0.1).toDouble()
    val shift_arb_on_logsum_pt_active: Double = (0).toDouble()
    val shift_arb_on_logsum_pt_active_fix: Double = (0).toDouble()
    val shift_arb_on_logsum_drive: Double = (0).toDouble()
    val shift_arb_on_logsum_drive_fix: Double = (0).toDouble()
    val shift_arb_on_attr: Double = (0 + 0.1).toDouble()
    val shift_educ_on_logsum_pt_active: Double = (0).toDouble()
    val shift_educ_on_logsum_pt_active_fix: Double = (0).toDouble()
    val shift_educ_on_logsum_drive: Double = (0).toDouble()
    val shift_educ_on_logsum_drive_fix: Double = (0).toDouble()
    val shift_educ_on_attr: Double = (0 - 0.05).toDouble()
    val shift_high_inc_on_logsum_pt_active: Double = (0).toDouble()
    val shift_high_inc_on_logsum_pt_active_fix: Double = (0).toDouble()
    val shift_high_inc_on_logsum_drive: Double = (0).toDouble()
    val shift_high_inc_on_logsum_drive_fix: Double = (0).toDouble()
    val shift_high_inc_on_attr: Double = (0).toDouble()
    val shift_zk_on_logsum_pt_active: Double = (0).toDouble()
    val shift_zk_on_logsum_pt_active_fix: Double = (0).toDouble()
    val shift_zk_on_logsum_drive: Double = (0).toDouble()
    val shift_zk_on_logsum_drive_fix: Double = (0).toDouble()
    val shift_zk_on_attr: Double = (0).toDouble()
    val shift_carav_on_logsum_pt_active: Double = (0).toDouble()
    val shift_carav_on_logsum_pt_active_fix: Double = (0).toDouble()
    val shift_carav_on_logsum_drive: Double = (0).toDouble()
    val shift_carav_on_logsum_drive_fix: Double = (0).toDouble()
    val shift_carav_on_attr: Double = (0.1).toDouble()
    val shift_uml_on_logsum_pt_active: Double = (0.0640383675977011 + 0.1).toDouble()
    val shift_uml_on_logsum_pt_active_fix: Double = (0.0416507340946399 + 0.1).toDouble()
    val shift_uml_on_logsum_drive: Double = (0.0462160663655077 + 0.1).toDouble()
    val shift_uml_on_logsum_drive_fix: Double = (-0.0755006569431623 + 0.1).toDouble()
    val shift_uml_on_attr: Double = (0.610291409419243 - 0.2).toDouble()
    val max_attractivity: Double = (20000).toDouble()

    val actTypesBikeFix = doubleArrayOf(b_arb_on_rad, b_dienst_on_rad, b_freizeit_on_rad, b_service_on_rad, 0.0, 0.0)
    val actTypesCarFix = doubleArrayOf(b_arb_on_pkw, b_dienst_on_pkw, 0.0, b_service_on_pkw, b_home_on_pkw, 0.0)
    val actTypesMfFix = doubleArrayOf(b_arb_on_mf, b_dienst_on_mf, 0.0, b_service_on_mf, b_home_on_mf, 0.0)
    val actTypesPTFix = doubleArrayOf(b_arb_on_oev, b_dienst_on_oev, b_freizeit_on_oev, b_service_on_oev, 0.0, 0.0)
    val actTypesPedFix =
        doubleArrayOf(b_arb_on_fuss, b_dienst_on_fuss, b_freizeit_on_fuss, b_service_on_fuss, 0.0, 0.0)
    val distanceEffectsGlobal = doubleArrayOf(0.0, b_0_1, b_1_2, 0.0)
    val ageEffects = doubleArrayOf(
        0.0,
        shift_age_2_on_attr,
        shift_age_3_on_attr,
        shift_age_4_on_attr,
        shift_age_56_on_attr,
        shift_age_78_on_attr,
        0.0,
    )
    val employmentEffects = doubleArrayOf(shift_educ_on_attr, shift_arb_on_attr, 0.0)
    val distanceEffects = doubleArrayOf(0.0, shift_b_0_1_on_attr, shift_b_1_2_on_attr, 0.0)
    val ageEffectsPt = doubleArrayOf(
        0.0,
        shift_age_2_on_logsum_pt_active,
        shift_age_3_on_logsum_pt_active,
        shift_age_4_on_logsum_pt_active,
        shift_age_56_on_logsum_pt_active,
        shift_age_78_on_logsum_pt_active,
        0.0,
    )
    val employmentEffectsPt = doubleArrayOf(shift_educ_on_logsum_pt_active, shift_arb_on_logsum_pt_active, 0.0)
    val ageEffectsPtFix = doubleArrayOf(
        0.0,
        shift_age_2_on_logsum_pt_active_fix,
        shift_age_3_on_logsum_pt_active_fix,
        shift_age_4_on_logsum_pt_active_fix,
        shift_age_56_on_logsum_pt_active_fix,
        shift_age_78_on_logsum_pt_active_fix,
        0.0,
    )
    val employmentEffectsPtFix =
        doubleArrayOf(shift_educ_on_logsum_pt_active_fix, shift_arb_on_logsum_pt_active_fix, 0.0)
    val ageEffectsDriveFix = doubleArrayOf(
        0.0,
        shift_age_2_on_logsum_drive_fix,
        shift_age_3_on_logsum_drive_fix,
        shift_age_4_on_logsum_drive_fix,
        shift_age_56_on_logsum_drive_fix,
        shift_age_78_on_logsum_drive_fix,
        0.0,
    )
    val employmentEffectsDriveFix = doubleArrayOf(shift_educ_on_logsum_drive_fix, shift_arb_on_logsum_drive_fix, 0.0)
    val ageEffectsDrive = doubleArrayOf(
        0.0,
        shift_age_2_on_logsum_drive,
        shift_age_3_on_logsum_drive,
        shift_age_4_on_logsum_drive,
        shift_age_56_on_logsum_drive,
        shift_age_78_on_logsum_drive,
        0.0,
    )
    val employmentEffectsDrive = doubleArrayOf(shift_educ_on_logsum_drive, shift_arb_on_logsum_drive, 0.0)
}
