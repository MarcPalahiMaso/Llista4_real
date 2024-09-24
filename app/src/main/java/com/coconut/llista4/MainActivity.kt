package com.coconut.llista4

import android.content.Context
import com.coconut.llista4.classes.Llibre
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coconut.llista4.ui.theme.Llista4Theme
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    private lateinit var biblioteca: MutableList<Llibre>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        biblioteca = mutableListOf() // Inicialitza la biblioteca

        // Llegeix les dades dels llibres al començar
        biblioteca = llegeix("Llibres.csv", this)

        setContent {
            Llista4Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainMenu(innerPadding)
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Desa les dades dels llibres al tancar l'aplicació
        desa("Llibres.csv", biblioteca)
    }

    // Funcions del menú principal
    @Composable
    fun MainMenu(innerPadding: PaddingValues) {
        var inputText by remember { mutableStateOf("") }
        var posicioInici by remember { mutableStateOf("") }
        var posicioFi by remember { mutableStateOf("") }
        var resultText by remember { mutableStateOf("") } // Estat per al resultat

        // Utilitza un ScrollState per a la columna desplaçable
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState) // Permet el desplaçament vertical
        ) {
            Text(text = "Gestió de la Biblioteca", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                resultText = llistaPaisos(biblioteca)
            }) {
                Text(text = "Llistar Països")
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        if (inputText.isEmpty()) Text("Tot lo que no sigui posició inici o fi, va aquí (IBNE, PAIS, ETC)")
                        innerTextField()
                    }
                }
            )

            Button(onClick = {
                resultText = llistaPais(inputText, biblioteca)
            }) {
                Text(text = "Llistar Llibres per País")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                resultText = llistaIdioma(inputText, biblioteca)
            }) {
                Text(text = "Llistar Llibres per Idioma")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                resultText = llistaLlibre(inputText, biblioteca)
            }) {
                Text(text = "Llistar Llibre per idBNE")
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = posicioInici,
                onValueChange = { posicioInici = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        if (posicioInici.isEmpty()) Text("Escriu la posició d'inici")
                        innerTextField()
                    }
                }
            )

            BasicTextField(
                value = posicioFi,
                onValueChange = { posicioFi = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        if (posicioFi.isEmpty()) Text("Escriu la posició final")
                        innerTextField()
                    }
                }
            )

            Button(onClick = {
                val inici = posicioInici.toIntOrNull() ?: 0
                val fi = posicioFi.toIntOrNull() ?: 0
                resultText = llistaRang(inici, fi, biblioteca)
            }) {
                Text(text = "Llistar Llibres per Rang de Posicions")
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = posicioInici,
                onValueChange = { posicioInici = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )

            Button(onClick = {
                val posicio = posicioInici.toIntOrNull() ?: -1
                eliminaPosicio(posicio, biblioteca)
                resultText = "Llibre eliminat per posició: $posicio"
            }) {
                Text(text = "Eliminar Llibre per Posició")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                eliminaLlibre(inputText, biblioteca)
                resultText = "Llibre eliminat per idBNE: $inputText"
            }) {
                Text(text = "Eliminar Llibre per idBNE")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostra el resultat
            Text(text = resultText, modifier = Modifier.padding(8.dp).fillMaxWidth())
        }
    }

    // Funció per separar
    fun separa(text: String): List<String> {
        return text.split(" // ")
    }

    // Funció per convertir una línia del CSV en un llibre
    fun converteix(text: String): Llibre {
        val camps = text.split(";")
        return Llibre(
            idBNE = camps[0],
            autorPersones = separa(camps[1]).toMutableList(),
            autorEntitats = separa(camps[2]).toMutableList(),
            titol = camps[3],
            descripcio = camps[4],
            genere = camps[5],
            dipositLegal = camps[6],
            pais = camps[7],
            idioma = camps[8],
            versioDigital = camps[9],
            textOCR = camps[10],
            isbn = camps[11],
            tema = camps[12],
            editorial = camps[13],
            llocPublicacio = camps[14]
        )
    }

    // Funció per llegir fitxers CSV
    fun llegeix(nomFitxer: String, context: Context): MutableList<Llibre> {
        val llibres = mutableListOf<Llibre>()
        val inputStream = context.assets.open(nomFitxer)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        bufferedReader.forEachLine { linia ->
            val llibre = converteix(linia)
            llibres.add(llibre)
        }
        bufferedReader.close() // Tanca el BufferedReader
        return llibres
    }

    // Funció per desar els llibres a un fitxer CSV
    fun desa(nomFitxer: String, biblioteca: List<Llibre>) {
        val fitxer = File(nomFitxer)
        val fileWriter = FileWriter(fitxer)
        biblioteca.forEach { llibre ->
            val linia = "${llibre.idBNE};${llibre.autorPersones.joinToString(" // ")};" +
                    "${llibre.autorEntitats.joinToString(" // ")};${llibre.titol};${llibre.descripcio};" +
                    "${llibre.genere};${llibre.dipositLegal};${llibre.pais};${llibre.idioma};${llibre.versioDigital};" +
                    "${llibre.textOCR};${llibre.isbn};${llibre.tema};${llibre.editorial};${llibre.llocPublicacio}\n"
            fileWriter.write(linia)
        }
        fileWriter.close()
    }

    // Funció per donar d'alta un llibre
    fun altaLlibre(llibre: Llibre, biblioteca: MutableList<Llibre>) {
        val existent = biblioteca.find { it.idBNE == llibre.idBNE }
        if (existent != null) {
            biblioteca.remove(existent)
        }
        biblioteca.add(llibre)
    }

    // Funció per eliminar un llibre per idBNE
    fun eliminaLlibre(idBNE: String, biblioteca: MutableList<Llibre>) {
        biblioteca.removeIf { it.idBNE == idBNE }
    }

    // Funció per eliminar un llibre per posició
    fun eliminaPosicio(posicio: Int, biblioteca: MutableList<Llibre>) {
        if (posicio in biblioteca.indices) {
            biblioteca.removeAt(posicio)
        }
    }

    // Funció per llistar els països únics
    fun llistaPaisos(biblioteca: List<Llibre>): String {
        val paisos = biblioteca.map { it.pais }.distinct()
        return paisos.joinToString(", ")
    }

    // Funció per llistar llibres d'un país
    fun llistaPais(pais: String, biblioteca: List<Llibre>): String {
        val llibres = biblioteca.filter { it.pais == pais }
        return if (llibres.isNotEmpty()) llibres.joinToString("\n") else "No hi ha llibres d'aquest país."
    }

    // Funció per llistar idiomes únics
    fun llistaIdiomes(biblioteca: List<Llibre>): String {
        val idiomes = biblioteca.map { it.idioma }.distinct()
        return idiomes.joinToString(", ")
    }

    // Funció per llistar llibres d'un idioma
    fun llistaIdioma(idioma: String, biblioteca: List<Llibre>): String {
        val llibres = biblioteca.filter { it.idioma == idioma }
        return if (llibres.isNotEmpty()) llibres.joinToString("\n") else "No hi ha llibres en aquest idioma."
    }

    // Funció per llistar un llibre per idBNE
    fun llistaLlibre(idBNE: String, biblioteca: List<Llibre>): String {
        val llibre = biblioteca.find { it.idBNE == idBNE }
        return if (llibre != null) llibre.toString() else "No s'ha trobat cap llibre amb l'idBNE: $idBNE"
    }

    // Funció per llistar llibres entre un rang de posicions
    fun llistaRang(inici: Int, fi: Int, biblioteca: List<Llibre>): String {
        return if (inici in biblioteca.indices && fi in biblioteca.indices && inici <= fi) {
            biblioteca.subList(inici, fi + 1).joinToString("\n")
        } else {
            "Rang fora de límits o incorrecte"
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Llista4Theme {
        Greeting("Android")
    }
}
