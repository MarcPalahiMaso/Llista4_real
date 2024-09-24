package com.coconut.llista4.classes

data class Llibre(
    var idBNE: String,
    var autorPersones: MutableList<String>,
    var autorEntitats: MutableList<String>,
    var titol: String,
    var descripcio: String,
    var genere: String,
    var dipositLegal: String,
    var pais: String,
    var idioma: String,
    var versioDigital: String,
    var textOCR: String,
    var isbn: String,
    var tema: String,
    var editorial: String,
    var llocPublicacio: String
)
