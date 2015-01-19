# songku

This is a web app that finds haiku by checking all combinations of
song titles by a particular musician.

You can see it in action at [http://songku.herokuapp.com/](http://songku.herokuapp.com/).


## Development

To start a local web server for development you can either eval the
commented out forms at the bottom of `web.clj` from your editor or
launch from the command line:

    $ lein run -m songku.web


## Deployment

Once you've installed the heroku toolbelt and associated this project
with a heroku app, you can deploy new code with

```
$ git push heroku master
```


## License

Copyright © 2015 John Wiseman jjwiseman@gmail.com

Distributed under the MIT License.
