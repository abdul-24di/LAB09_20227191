// Todo el codigo jQuery dentro de ready(): asegura que el DOM ya cargo (clase 10.3, diapo 5)
$(document).ready(function () {

    const API = "/api/puzzle";
    // NxN con el mismo numero a ambos lados (3x3 ... 9x9)
    const REGEX_DIMENSION = /^([3-9])[xX]\1$/;

    let n = 0;                 // dimension actual
    let tablero = [];          // ej. ["4","1","3","2","8","5","7","6","X"]
    let bloqueado = false;     // evita clics mientras se muestra "Juego terminado !"
    let colorTablero = "#ffffff";
    // ?t=... evita que el navegador use una imagen vieja en cache
    let imgUrl = API + "/imagen?t=" + Date.now();

    // ---------- Utilidades ----------

    function leer(estructura) {
        return estructura.indexOf("-") >= 0 ? estructura.split("-") : estructura.split("");
    }

    function ordenado(n) {
        const t = [];
        for (let i = 1; i < n * n; i++) t.push(String(i));
        t.push("X");
        return t;
    }

    function mostrarMensaje(texto, ok) {
        $("#mensaje").removeClass("text-success text-danger")
            .addClass(ok ? "text-success" : "text-danger")
            .text(texto);
    }

    function errorDe(xhr) {
        return (xhr.responseJSON && xhr.responseJSON.msg)
            ? xhr.responseJSON.msg
            : "No se pudo conectar con el servidor";
    }

    // Dos casillas son contiguas si estan a distancia 1 (misma fila o columna)
    function esContigua(ficha) {
        const i = tablero.indexOf(ficha);
        const v = tablero.indexOf("X");
        return Math.abs(Math.floor(i / n) - Math.floor(v / n)) + Math.abs(i % n - v % n) === 1;
    }

    // ---------- Dibujar el tablero ----------
    // Cada ficha k usa la MISMA imagen como fondo, desplazada a su recorte:
    // tamano = N*100% y posicion = columna/fila de la ficha en la imagen ordenada
    function dibujar() {
        const $t = $("#tablero").empty().css({
            "display": "grid",
            "grid-template-columns": "repeat(" + n + ", 1fr)",
            "grid-template-rows": "repeat(" + n + ", 1fr)"
        });

        tablero.forEach(function (valor) {
            if (valor === "X") {
                $t.append($("<div>").addClass("celda vacia").css("background-color", colorTablero));
                return;
            }
            const k = parseInt(valor, 10) - 1;
            const fila = Math.floor(k / n);
            const col = k % n;
            $t.append($("<div>").addClass("celda ficha").attr("data-ficha", valor).css({
                "background-image": "url('" + imgUrl + "')",
                "background-size": (n * 100) + "% " + (n * 100) + "%",
                "background-position": (col * 100 / (n - 1)) + "% " + (fila * 100 / (n - 1)) + "%"
            }));
        });
    }

    // ---------- Validar dimension: mostrar u ocultar "Iniciar el juego" ----------
    function validarDimension() {
        const ok = REGEX_DIMENSION.test($("#dimension").val().trim());
        if (ok) {
            $("#btnIniciar").show();
            $("#errorDimension").hide();
        } else {
            $("#btnIniciar").hide();
            $("#errorDimension").show();
        }
        return ok;
    }

    // ---------- Estado inicial ----------

    $("#preview").attr("src", imgUrl);

    // El color es solo visual: se recuerda en el navegador (la tabla solo admite 2 columnas)
    try {
        const c = localStorage.getItem("stfColorTablero");
        if (c) colorTablero = c;
    } catch (e) { /* navegador sin localStorage: se usa blanco */ }
    $("#color").val(colorTablero);

    validarDimension();

    // Retomar el juego guardado en BD (GET /api/puzzle)
    $.ajax({
        url: API,
        method: "GET",
        dataType: "json"
    }).done(function (r) {
        if (r.enJuego) {
            n = r.n;
            tablero = leer(r.estructura);
            $("#dimension").val(n + "x" + n);
            validarDimension();
            dibujar();
            mostrarMensaje("Juego retomado", true);
        }
    }).fail(function (xhr) {
        mostrarMensaje(errorDe(xhr), false);
    });

    // ---------- Eventos ----------

    $("#dimension").on("input", validarDimension);

    $("#color").on("input", function () {
        colorTablero = $(this).val();
        try { localStorage.setItem("stfColorTablero", colorTablero); } catch (e) { }
        $(".vacia").css("background-color", colorTablero);
    });

    // "Seleccionar Personaje" abre el explorador de archivos del sistema operativo
    $("#btnPersonaje").on("click", function () {
        $("#archivo").click();
    });

    // Subir la imagen por AJAX (POST /api/puzzle/imagen) sin recargar la pagina
    $("#archivo").on("change", function () {
        const archivo = this.files[0];
        $(this).val("");                     // permite volver a elegir el mismo archivo
        if (!archivo) return;

        if (!/\.jpe?g$/i.test(archivo.name)) {
            mostrarMensaje("Solo se permiten imágenes en formato jpg", false);
            return;
        }

        const formData = new FormData();
        formData.append("archivo", archivo);

        $.ajax({
            url: API + "/imagen",
            method: "POST",
            data: formData,
            processData: false,           // FormData se envia tal cual (multipart)
            contentType: false,           // el navegador pone multipart/form-data con su boundary
            dataType: "json"
        }).done(function () {
            imgUrl = API + "/imagen?t=" + Date.now();
            $("#preview").attr("src", imgUrl);
            if (n > 0) dibujar();             // si hay juego, las fichas cambian a la nueva imagen
            mostrarMensaje("Personaje actualizado", true);
        }).fail(function (xhr) {
            mostrarMensaje(errorDe(xhr), false);
        });
    });

    // Iniciar juego (POST /api/puzzle/iniciar)
    $("#btnIniciar").on("click", function () {
        if (!validarDimension()) return;

        $.ajax({
            url: API + "/iniciar",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({ dimension: $("#dimension").val().trim() }),
            dataType: "json"
        }).done(function (r) {
            n = r.n;
            tablero = leer(r.estructura);
            bloqueado = false;
            dibujar();
            mostrarMensaje("", true);
        }).fail(function (xhr) {
            mostrarMensaje(errorDe(xhr), false);
        });
    });

    // Mover ficha (PUT /api/puzzle/mover)
    // Delegacion con .on(): las fichas se crean dinamicamente, un .click() normal
    // no les responderia (clase 10.3, diapos 12-13)
    $("#tablero").on("click", ".ficha", function () {
        if (bloqueado) return;

        const ficha = $(this).attr("data-ficha");
        if (!esContigua(ficha)) {
            mostrarMensaje("Solo puedes mover fichas contiguas a la casilla vacía", false);
            return;
        }

        $.ajax({
            url: API + "/mover",
            method: "PUT",
            contentType: "application/json",
            data: JSON.stringify({ ficha: ficha }),
            dataType: "json"
        }).done(function (r) {
            if (r.estado === "terminado") {
                // Muestra la imagen armada y el mensaje; la BD ya tiene el juego nuevo
                tablero = ordenado(n);
                dibujar();
                mostrarMensaje("Juego terminado !", true);
                bloqueado = true;
                setTimeout(function () {
                    n = r.n;
                    tablero = leer(r.estructura);
                    dibujar();
                    mostrarMensaje("Nuevo juego iniciado", true);
                    bloqueado = false;
                }, 2500);
            } else {
                tablero = leer(r.estructura);
                dibujar();
                mostrarMensaje("", true);
            }
        }).fail(function (xhr) {
            mostrarMensaje(errorDe(xhr), false);
        });
    });
});