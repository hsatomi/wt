﻿window.onload = function () {
    // ページ読み込み時に実行したい処理
    document.getElementById("statistics_information").style.width = window.parent.screen.width * 0.4 + "px";
    document.getElementById("statistics_information").style.height = window.parent.screen.height * 0.8 + "px";

    document.getElementById("project_list").style.width = window.parent.screen.width * 0.5 + "px";
    document.getElementById("project_list").style.height = window.parent.screen.height * 0.8 + "px";
}

function move(mode) {
    switch (mode) {
        case "function": location.href = "function_list.jsp"; break;
        default: break;
    }
}