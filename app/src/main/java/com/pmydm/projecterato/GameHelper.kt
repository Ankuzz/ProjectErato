package com.pmydm.projecterato

import android.content.Context
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.random.Random

data class Country(
    val name: String,
    val capital: String,
    val flag: Flag
)

data class Flag(
    val path: String
)


class GameHelper(val gameType: String, val region: String, val context: Context) {

    private var countriesList: MutableList<Country> = mutableListOf()
    private var secondaryCountriesList: MutableList<Country> = mutableListOf()
    private var flagsList: MutableList<String> = mutableListOf()
    private var countryNameOrCapital: String = ""
    private var countriesShownCount: Int = 0

    init {
        loadGameData()
    }

    private fun loadGameData() {
        val countries = loadCountriesForRegion(region)
        countriesList = countries.toMutableList()
        secondaryCountriesList = countries.toMutableList()
    }

    private fun getButtonState(): String {
        val sharedPreferences = context.getSharedPreferences("prefs_file", Context.MODE_PRIVATE)
        return sharedPreferences.getString("button_state", "infinito") ?: "infinito"
    }

    fun isGameOver(fallos: Int): Boolean {
        val buttonState = getButtonState()

        return when (buttonState) {
            "infinito" -> countriesList.isEmpty()  // Juego termina cuando no hay más países
            "10" -> countriesShownCount >= 10  // Juego termina después de 10 países mostrados
            "alfallo" -> (fallos > 0 || countriesList.isEmpty()) // Juego termina si se falla
            else -> countriesList.isEmpty()
        }
    }


    fun getNextQuestion() {
        if (countriesList.isEmpty()) return // Termina el juego si no quedan países

        val mainCountry = countriesList.random()



        countryNameOrCapital = when (gameType) {
            "Paises" -> mainCountry.name
            "Capitales" -> mainCountry.capital
            "Mixto" -> {
                if (Random.nextBoolean()) mainCountry.name else mainCountry.capital
            }
            else -> mainCountry.name  // Valor por defecto en caso de que gameType no sea reconocido
        }


        countriesList.remove(mainCountry)
        flagsList.clear()
        flagsList.add(mainCountry.flag.path) // Agregar la bandera correcta

        // Crear una copia mutable de la lista secundaria para evitar duplicados
        val tempSecondaryCountries = secondaryCountriesList.toMutableList()
        tempSecondaryCountries.remove(mainCountry) // Eliminar el país correcto de la lista

        // Seleccionar 3 países incorrectos únicos
        val incorrectCountries = tempSecondaryCountries.shuffled().take(3)

        incorrectCountries.forEach { country ->
            flagsList.add(country.flag.path)
        }

        // Aumentar el contador de países mostrados
        countriesShownCount++
    }


    private fun loadCountriesForRegion(region: String): List<Country> {
        Log.d("GameHelper", "Región solicitada: $region") // Verificar la región solicitada

        return when (region) {
            "Africa" -> {
                Log.d("GameHelper", "Cargando datos de África desde africa_paises.xml")
                loadCountriesFromXML("africa_paises.xml")
            }
            "America" -> {
                Log.d("GameHelper", "Cargando datos de América desde america_paises.xml")
                loadCountriesFromXML("america_paises.xml")
            }
            "Asia" -> {
                Log.d("GameHelper", "Cargando datos de Asia desde asia_paises.xml")
                loadCountriesFromXML("asia_paises.xml")
            }
            "Europa" -> {
                Log.d("GameHelper", "Cargando datos de Europa desde europa_paises.xml")
                loadCountriesFromXML("europa_paises.xml")
            }
            "Oceania" -> {
                Log.d("GameHelper", "Cargando datos de Oceanía desde oceania_paises.xml")
                loadCountriesFromXML("oceania_paises.xml")
            }
            else -> {
                Log.e("GameHelper", "Región desconocida: $region. Devolviendo lista vacía.")
                emptyList()
            }
        }
    }

    fun clearGameState() {
        // Limpiar las listas de países
        countriesList.clear()
        secondaryCountriesList.clear()

        // Limpiar la lista de banderas
        flagsList.clear()

        // Resetear el nombre o la capital actual
        countryNameOrCapital = ""

        // Recargar los datos para que el siguiente juego comience de nuevo
        loadGameData()
    }


    private fun loadCountriesFromXML(fileName: String): List<Country> {
        val countryList = mutableListOf<Country>()

        try {
            // Obtener el código de idioma desde las preferencias
            val sharedPreferences = context.getSharedPreferences("prefs_file", Context.MODE_PRIVATE)
            val languageCode = sharedPreferences.getString("language_code", "es") ?: "es" // Por defecto a "es"

            val parserFactory = XmlPullParserFactory.newInstance()
            val parser = parserFactory.newPullParser()

            // Cargar el archivo XML desde los assets
            val inputStream = context.assets.open(fileName)
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            var currentCountry: Country? = null
            var flagPath = ""
            var name = ""
            var capital = ""
            var insideName = false
            var insideCapital = false

            // Leer el archivo XML
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "country" -> {
                                currentCountry = Country(name = "", capital = "", flag = Flag(path = ""))
                            }
                            "name" -> insideName = true
                            "capital" -> insideCapital = true
                            "default", "en", "de", "pl" -> {
                                if (parser.name == languageCode || (languageCode == "es" && parser.name == "default")) {
                                    if (insideName) name = parser.nextText()
                                    if (insideCapital) capital = parser.nextText()
                                }
                            }
                            "flag" -> flagPath = parser.getAttributeValue(null, "path")
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "name" -> insideName = false
                            "capital" -> insideCapital = false
                            "country" -> {
                                if (currentCountry != null) {
                                    currentCountry = currentCountry.copy(
                                        name = name,
                                        capital = capital,
                                        flag = Flag(path = flagPath)
                                    )
                                    countryList.add(currentCountry)
                                    currentCountry = null
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("GameHelper", "Error al cargar el archivo $fileName: ${e.message}")
            e.printStackTrace()
        }

        return countryList
    }



    fun getFlagsList(): List<String> {
        return flagsList
    }

    fun getCountryNameOrCapital(): String {
        return countryNameOrCapital
    }
}
