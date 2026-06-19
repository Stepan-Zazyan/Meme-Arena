import 'api_config.dart';
class ContentUrlResolver{ContentUrlResolver(this.config);final ApiConfig config;String resolve(String? url){final v=(url??'').trim();if(v.isEmpty)return v;final u=Uri.tryParse(v);if(u!=null&&u.hasScheme&&(u.scheme=='http'||u.scheme=='https'))return v;if(v.startsWith('/'))return '${config.baseUrl}$v';return '${config.baseUrl}/$v';}}
