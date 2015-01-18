$(function() {

  function renderSuggestion(suggestion) {
    var entity = suggestion.value;
    return '<div class="name">' + entity.name + '</div>';
  }

  var params = $.param({
    filter: '(all type:/music/artist type:/music/artist type:music/artist)',
    spell: 'always',
    exact: 'false',
    prefixed: 'true',
    limit: 5,
    key: 'AIzaSyBgs8886GdJh0iOQq0XCSzEAzlSONY1R7c'});
  console.log('PARAMS');
  console.log(params);
  musicSuggester = new Bloodhound({
    datumTokenizer: function (d) {
      return Bloodhound.tokenizers.whitespace(d.value);
    },
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    remote: {
      url: ('https://www.googleapis.com/freebase/v1/search?query=%QUERY&' +
            params),
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
