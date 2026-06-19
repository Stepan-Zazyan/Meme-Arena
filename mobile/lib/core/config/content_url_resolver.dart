import 'api_config.dart';

class ContentUrlResolver {
  ContentUrlResolver(this.config);
  final ApiConfig config;

  String resolve(String? url) {
    if (url == null || url.isEmpty) return '';
    final uri = Uri.tryParse(url);
    if (uri != null && (uri.scheme == 'http' || uri.scheme == 'https')) return url;
    final path = url.replaceFirst(RegExp(r'^/+'), '');
    return '${config.baseUrl}/$path';
  }
}
