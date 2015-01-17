$(function() {

  function renderSuggestion(suggestion) {
    var entity = suggestion.value;
    return '<div class="name">' + entity.name + '</div>';
  }

  musicSuggester = new Bloodhound({
    datumTokenizer: function (d) {
      return Bloodhound.tokenizers.whitespace(d.value);
    },
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    remote: {
      url: ('https://www.googleapis.com/freebase/v1/search?query=%QUERY' +
            '&filter=(all+type%3A%2Fmusic%2Fartist+type%3A%2Fmusic%2Fartist' +
            '+type%3A%2Fmusic%2Fartist+type%3A%2Fmusic%2Fartist+type%3A%2F' +
            'music%2Fartist+type%3A%2Fmusic%2Fartist+type%3A%2Fmusic%2Fartist' +
            '+type%3A%2Fmusic%2Fartist)' +
            '&spell=always' +
            '&exact=false' +
            '&prefixed=true' +
            '&limit=5'),
      filter: function (music) {
        return $.map(music.result, function (entity) {
          var shortName = entity['name'];
          // if (shortName.length > 48) {
          //   shortName = shortName.substr(0, 45) + '...';
          // }
          entity.shortName = shortName;
          return {
            displayName: entity['name'],
            value: entity
          };
        });
      }
    }
  });

  // initialize the bloodhound suggestion engine
  musicSuggester.initialize();
});
