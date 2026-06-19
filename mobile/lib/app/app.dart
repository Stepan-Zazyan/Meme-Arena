import 'package:flutter/material.dart';
import 'router.dart';import 'theme.dart';
class MemeArenaApp extends StatelessWidget{const MemeArenaApp({super.key});@override Widget build(BuildContext context)=>MaterialApp(title:'Meme Arena',debugShowCheckedModeBanner:false,theme:darkTheme,home:const AppRoot());}
