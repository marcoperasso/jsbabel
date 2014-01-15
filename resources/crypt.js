//0
this.crypt = function(s){
    var ret = '';
    for (var i = 0; i < s.length; i++){
        var code = s.charCodeAt(i) + 1;
        ret += String.fromCharCode(code);
    }
    return ret;
}
//1
this.crypt = function(s){
    var ret = '';
    for (var i = 0; i < s.length; i++){
        var code = s.charCodeAt(i) -1;
        ret += String.fromCharCode(code);
    }
    return ret;
}
//2
this.crypt = function(s){
    var ret = '';
    for (var i = 0; i < s.length; i=i+2){
        ret += s.charAt(i);
    }
    for (var i = 1; i < s.length; i=i+2){
        ret += s.charAt(i);
    }
    return ret;
}
//3
this.crypt = function(s){
    var ret = '';
    for (var i = 1; i < s.length; i=i+2){
        ret += s.charAt(i);
    }
    for (var i = 0; i < s.length; i=i+2){
        ret += s.charAt(i);
    }
    return ret;
}