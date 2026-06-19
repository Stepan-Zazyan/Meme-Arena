class ApiConfig{ApiConfig({String? baseUrl}):baseUrl=(baseUrl??const String.fromEnvironment('API_BASE_URL',defaultValue:'http://10.0.2.2:8080')).replaceFirst(RegExp(r'/+$'),'');final String baseUrl;}
