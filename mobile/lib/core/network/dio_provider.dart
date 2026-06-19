import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../config/api_config.dart';

final apiConfigProvider = Provider((_) => ApiConfig());
final dioProvider = Provider((ref) {
  final dio = Dio(BaseOptions(
    baseUrl: ref.watch(apiConfigProvider).baseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 20),
    sendTimeout: const Duration(seconds: 20),
  ));
  dio.interceptors.add(InterceptorsWrapper(
    onRequest: (options, handler) {
      options.headers['X-Request-Id'] = 'ma-${DateTime.now().microsecondsSinceEpoch}';
      if (kDebugMode) debugPrint('${options.method} ${options.uri} requestId=${options.headers['X-Request-Id']}');
      handler.next(options);
    },
    onResponse: (response, handler) {
      if (kDebugMode) debugPrint('${response.statusCode} ${response.requestOptions.uri} requestId=${response.headers.value('X-Request-Id')}');
      handler.next(response);
    },
  ));
  return dio;
});
