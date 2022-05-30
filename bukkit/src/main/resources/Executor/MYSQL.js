var Object = Java.type('java.lang.Object');

var validation = {
  overloads: [
    [
      { type: 'string', name: 'key' },
      { type: Object.class, name: 'value' },
    ],
  ],
};

function MYSQL(args) {
  var mysqlHelper = plugin.getMysqlHelper();
  if (mysqlHelper === null)
    throw new Error('Mysql connection is not available. Check your config.yml');

  var key = args[0];
  var value = args[1];

  mysqlHelper.set(key, value);
}
