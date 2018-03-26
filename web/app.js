const express = require('express');
const path = require('path');
const sqlite3 = require('sqlite3').verbose();
const parse = require('csv-parse/lib/sync');
const app = express();
const bodyParser = require('body-parser');
const fs = require('fs');

var db = new sqlite3.Database(':memory:');

db.serialize(function() {
  let schema =
    ['restaurant_name TEXT',
      'item_name TEXT',
      'item_type INTEGER',
      'calories INTEGER'];

  db.run('CREATE TABLE food (' + schema.join(',') + ')');

  var stmt = db.prepare('INSERT INTO food VALUES (' + Array(schema.length).fill('?').join(', ') + ')');

  var food_data = require('fs').readFileSync(path.join(__dirname, 'food.csv'), 'utf-8');
  var foods = parse(food_data, { delimiter: ';' });

  for (food of foods) {
    stmt.run(food);
  }

  stmt.finalize();
});

app.use(bodyParser.json());

app.post('/search', function(req, res) {
  console.log('Got request to search');
  console.log('Search parameters:', req.body);

  let query = 'SELECT * FROM food';

  let where = [];

  if (req.body.hasOwnProperty('restaurants') && req.body.restaurants.length > 0) {
    where.push('restaurant_name IN (' + req.body.restaurants.map(x => '"' + x + '"').join(',') + ')');
  }

  if (req.body.hasOwnProperty('item_type') && req.body.item_type > 0) {
    where.push('item_type = ' + req.body.item_type);
  }

  if (req.body.hasOwnProperty('fitness_goal') && req.body.hasOwnProperty('calories')) {
    if (req.body.fitness_goal == 0) {
      where.push('calories >= ' + (req.body.calories - 50));
      where.push('calories <= ' + (req.body.calories + 50));
    }
    else if (req.body.fitness_goal == 1) {
      where.push('calories >= ' + req.body.calories);
    }
    else if (req.body.fitness_goal == 2) {
      where.push('calories <= ' + req.body.calories);
    }
  }

  if (where.length > 0) {
    query = query + ' WHERE ' + where.join(' AND ');
  }

  query = query + ' LIMIT 15';

  console.log('Query:', query);

  db.all(query, function(err, rows) {
    console.log('Got results');
    console.log('Result is array:', Array.isArray(rows));
    console.log('Number of results:', rows.length);
    if (err) {
      console.log('Error:', err);
      res.json([]);
    }
    else {
      res.json(rows);
    }
  });
});

app.listen(3000, () => console.log('Food DB listening on port 3000!'));
