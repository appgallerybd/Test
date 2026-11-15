package com.safetube.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val bg=Color(0xFF0B111A)
private val panel=Color(0xFF151F2C)
private val accent=Color(0xFFE53935)
private val mint=Color(0xFF55D6BE)
private val muted=Color(0xFF9BAABD)
data class Clip(val url:String,val title:String,val fav:Boolean=false)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{SafeTube()}}
}
@Composable fun SafeTube(){
 val ctx= LocalContext.current
 val prefs= remember{ctx.getSharedPreferences("safetube",0)}
 fun load():List<Clip> = try {
  val a=JSONArray(prefs.getString("clips","[]")?:"[]")
  (0 until a.length()).map{val o=a.getJSONObject(it);Clip(o.getString("url"),o.optString("title","YouTube video"),o.optBoolean("fav"))}
 }catch(_:Exception){emptyList()}
 var clips by remember{mutableStateOf(load())}
 var link by remember{mutableStateOf("")};var name by remember{mutableStateOf("")};var search by remember{mutableStateOf("")}
 var favOnly by remember{mutableStateOf(false)};var note by remember{mutableStateOf("")}
 fun save(list:List<Clip>){val a=JSONArray();list.forEach{a.put(JSONObject().put("url",it.url).put("title",it.title).put("fav",it.fav))};prefs.edit().putString("clips",a.toString()).apply();clips=list}
 fun id(s:String):String?=try{val u=Uri.parse(s.trim());when{
  u.host?.contains("youtu.be")==true->u.lastPathSegment
  u.host?.contains("youtube.com")==true && u.path=="/watch"->u.getQueryParameter("v")
  u.host?.contains("youtube.com")==true && u.pathSegments.firstOrNull() in listOf("shorts","embed","live")->u.pathSegments.getOrNull(1)
  else->null
 }?.takeIf{it.matches(Regex("[A-Za-z0-9_-]{6,}"))}}catch(_:Exception){null}
 MaterialTheme(colorScheme=darkColorScheme(primary=accent,background=bg,surface=panel)){
  Scaffold(containerColor=bg,bottomBar={NavigationBar(containerColor=panel){
   NavigationBarItem(selected=!favOnly,onClick={favOnly=false},icon={Icon(Icons.Default.Home,null)},label={Text("Library")})
   NavigationBarItem(selected=favOnly,onClick={favOnly=true},icon={Icon(Icons.Default.Favorite,null)},label={Text("Favourites")})
  }}){pad->
   Column(Modifier.fillMaxSize().background(bg).padding(pad).padding(horizontal=17.dp)){
    Spacer(Modifier.height(18.dp))
    Row(verticalAlignment=Alignment.CenterVertically){
     Box(Modifier.size(46.dp).background(accent,RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Icon(Icons.Default.PlayArrow,null,tint=Color.White,modifier=Modifier.size(32.dp))}
     Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text("SafeTube",fontSize=25.sp,fontWeight=FontWeight.Bold);Text("YOUR VIDEO CONTROL CENTER",fontSize=9.sp,color=muted,letterSpacing=1.3.sp)}
     Text("SAFE",color=mint,fontWeight=FontWeight.Bold,fontSize=11.sp)
    }
    Spacer(Modifier.height(22.dp))
    Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(20.dp)){
     Column(Modifier.padding(15.dp)){
      Text("ADD YOUTUBE VIDEO",color=mint,fontSize=10.sp,fontWeight=FontWeight.Bold)
      Spacer(Modifier.height(10.dp))
      OutlinedTextField(link,{link=it},modifier=Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("YouTube video link")})
      Spacer(Modifier.height(7.dp))
      OutlinedTextField(name,{name=it},modifier=Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("Video name (optional)")})
      Spacer(Modifier.height(10.dp))
      Button(onClick={
       val vid=id(link)
       if(vid==null)note="সঠিক YouTube লিংক দিন"
       else if(clips.any{id(it.url)==vid})note="ভিডিওটি আগে থেকেই আছে"
       else{save(listOf(Clip("https://www.youtube.com/watch?v=$vid",name.ifBlank{"YouTube video • "+vid.take(6)})+clips));link="";name="";note="লাইব্রেরিতে যোগ হয়েছে"}
      },modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=accent),shape=RoundedCornerShape(12.dp)){
       Icon(Icons.Default.Add,null);Spacer(Modifier.width(6.dp));Text("Add to Library",fontWeight=FontWeight.Bold)
      }
     }
    }
    Spacer(Modifier.height(14.dp))
    OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("Search your library")},leadingIcon={Icon(Icons.Default.Search,null)})
    Spacer(Modifier.height(12.dp))
    Text(if(favOnly)"FAVOURITES" else "SAVED VIDEOS",color=muted,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.2.sp)
    Spacer(Modifier.height(7.dp))
    val shown=clips.filter{(!favOnly||it.fav)&&(it.title.contains(search,true)||it.url.contains(search,true))}
    if(shown.isEmpty())Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("এখনো কোনো ভিডিও যোগ করা হয়নি",color=muted)}
    else LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(bottom=15.dp)){
     items(shown,key={it.url}){v->Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(15.dp)){
      Row(Modifier.fillMaxWidth().padding(9.dp),verticalAlignment=Alignment.CenterVertically){
       Icon(Icons.Default.PlayCircle,null,tint=accent,modifier=Modifier.size(34.dp));Spacer(Modifier.width(8.dp))
       Column(Modifier.weight(1f)){Text(v.title,fontWeight=FontWeight.SemiBold,fontSize=13.sp);Text("YouTube",fontSize=10.sp,color=muted)}
       IconButton(onClick={val i=id(v.url);if(i!=null)ctx.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com/watch?v=$i")))} ){Icon(Icons.Default.PlayArrow,null,tint=mint)}
       IconButton(onClick={save(clips.map{if(it.url==v.url)it.copy(fav=!it.fav)else it})}){Icon(if(v.fav)Icons.Default.Favorite else Icons.Default.FavoriteBorder,null,tint=accent)}
       IconButton(onClick={save(clips.filterNot{it.url==v.url})}){Icon(Icons.Default.DeleteOutline,null,tint=muted)}
      }
     }}
    }
    if(note.isNotBlank())Text(note,color=mint,fontSize=12.sp)
   }
  }
 }
}
