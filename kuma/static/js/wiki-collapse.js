
(function ($, win, doc) {
    'use strict';

    // is this a docs page?
    var isDocsPage = location.pathname.toString().indexOf('/en-US/docs/') === 0 ? true : false;
    if(!isDocsPage) {
        return;
    }

    // hide toc
    $('#toc').toggle();

    var $article = $('#wikiArticle');
    var $h2s = $article.find('h2');

    // starting at the first h2, hide everything not an h2
    $h2s.nextUntil('h2').toggle();

    $h2s.prepend('<i aria-hidden="true" class="icon-plus-circle"></i>');

    // make h2s clickable
    $h2s.on('click', function(){
        // toggle icon
        $(this).find('i:first-child').toggleClass('icon-minus-circle');
        $(this).find('i:first-child').toggleClass('icon-plus-circle');
        // show h2 siblings until next h2 reached
        $(this).nextUntil('h2').toggle();
    });

    $('#Browser_compatibility').click();
    $('#Browser_Compatibility').click();

}(jQuery, window, document));
