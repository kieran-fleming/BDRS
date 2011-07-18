// Display message on browser in same style as JSP approach
bdrs.message = {
    set: function(msg) {
        this.clear();
        this.append(msg);
    },
    append: function(msg) {
        this.getDom().append('<p class="message">'+msg+'</p>');
    },
    clear: function() {
        this.getDom().empty();
    },
    getDom: function() {
        return jQuery(".messages");
    }
};