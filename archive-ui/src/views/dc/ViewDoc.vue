<template>
  <el-container v-title :data-title="$t('application.name')">
    <div>
       <el-dialog v-dialogDrag 
      :title="$t('application.borrow')"
      :visible.sync="borrowDialogVisible"
      @close="borrowDialogVisible = false"
      width="95%"
      style="width:100%"
      custom-class="customWidth"
    >
      <Borrow :docData="borrowData"></Borrow>
      <!--
         <router-view @showOrHiden="showOrHiden" ref="ShowShopingCart"></router-view>
        -->
     </el-dialog>
</div>
    <el-header style="height: 40px;padding-top:8px;">
      <el-col :span="20" style="padding-top:8px;">
        {{doc.typeName}} &nbsp;&nbsp; {{doc.code}} &nbsp;&nbsp; {{doc.revision}} &nbsp;&nbsp; {{doc.title}}
      </el-col>
      <el-col :span="4" style="float:right; text-align:right;">
        <template v-if="docObj!=null">
          
          <template v-if="judgeDownload.showDownloadButton">

            <el-button size="mini" icon="el-icon-download" @click="download()">{{$t('application.download')}}</el-button>

          </template>

        </template>
      </el-col>
    </el-header>
    <el-main>
      <el-row>
        <el-col :span="22" class="doccontent">
           <template v-if="docObj==null">
             {{message}}
           </template>
           <InnerItemViewer v-else-if="viewerType==100" v-bind:id = "doc.id" v-bind:tableHeight="innerTableHeight"></InnerItemViewer>
           <template v-else-if="doc.permit<3">
             <div style="padding-top:40px;">
              {{$t('application.noPermit')}}
             </div>
           </template>
           <template v-else>
             <PdfViewer v-if="viewerType==1" v-bind:id="doc.id" v-bind:format="doc.format" v-bind:permitLevel="judgePrint.printPermit"></PdfViewer>
             <OfficeDocViewer v-else-if="viewerType==2" v-bind:id="doc.id" v-bind:format="doc.format"></OfficeDocViewer>
             <ImageViewer v-else-if="viewerType==3" v-bind:id="doc.id" v-bind:format="doc.format"></ImageViewer>
             <VideoPlayer v-else-if="viewerType==4" v-bind:id="doc.id" v-bind:format="doc.format"></VideoPlayer>
             <AudioPlayer v-else-if="viewerType==5" v-bind:id="doc.id" v-bind:format="doc.format"></AudioPlayer>
             <!-- <CADViewer v-else-if="viewerType==6" v-bind:id="doc.id" format="ocf"></CADViewer> -->
             <CADViewerHtml5 v-else-if="viewerType==6" v-bind:id="doc.id" format="ocf"></CADViewerHtml5>
             <!-- Begin -->
             <!-- <JTViewer v-else-if="viewerType==7" v-bind:id="doc.id" format="obj" :fileName="doc.C_IMPORT_NAME"></JTViewer>
             <ThreeDsViewer v-else-if="viewerType==8" v-bind:id="doc.id" ></ThreeDsViewer> -->
             <!-- End -->
             <!-- 以下是新的代码-->
             <ThreeDsViewer v-else-if="viewerType==7" v-bind:id="doc.id" ></ThreeDsViewer> 
             <div v-else-if="doc.contentSize==0" style="padding-top:40px;">
                {{$t('application.noE-File')}}
            </div>
             <div v-else style="padding-top:40px;">
              {{$t('application.CurrentFormat')}}{{doc.format}}{{$t('application.CantViewOnline')}}
              </div>
           </template>
        </el-col>
        <el-col :span="2" class="aside-rigth">
            <div style="padding-top:10px;">
              
            </div>
            <br/>
            <div>
              <template v-if="docObj!=null">
              <el-button type="primary" plain @click="menuClick($t('application.dcproper'))">{{$t('application.dcproper')}}</el-button><br/>
              <el-button type="primary" plain @click="menuClick($t('application.relationDC'))">{{$t('application.relationDC')}}</el-button><br/>
              <el-button type="primary" plain @click="menuClick($t('application.DCver'))">{{$t('application.DCver')}}</el-button><br/>
              <el-button type="primary" plain @click="menuClick($t('application.Rendition'))">{{$t('application.Rendition')}}</el-button><br/>
               <el-button type="primary" plain @click="menuClick($t('application.viewWorkflow'))">{{$t('application.viewWorkflow')}}</el-button><br/>
              <!-- <el-button type="primary" plain @click="menuClick('利用信息')">利用信息</el-button><br/> -->
              <template v-if="doc.typeName=='文件传递单'" >
                <el-button type="primary" plain @click="menuClick($t('application.TransferDoc'))">{{$t('application.TransferDoc')}}</el-button><br/>
              </template>
              <template v-if="revertType.indexOf(doc.typeName)!=-1">
                <el-button type="primary" plain @click="menuClick($t('application.replyDoc'))">{{$t('application.replyDoc')}}</el-button><br/>
              </template>
              <el-button type="primary" plain @click="menuClick($t('application.Attachment'))">{{$t('application.Attachment')}}</el-button><br/>
              <!--
              <el-button v-if="doc.typeName=='设计文件'" type="primary" plain @click="menuClick($t('application.Changeinfo'))">{{$t('application.change')}}( {{doc.changeCount}} )</el-button>
              -->
              </template>
            </div>
        </el-col>
      </el-row>
    </el-main>

    <el-dialog v-dialogDrag  :title="dialog.title" :visible.sync="dialog.visible" width="90%" :before-close="handleClose">      
      <template v-if="dialog.title==$t('application.dcproper')">
        <ShowPropertyReadOnly ref="ShowProperty" :itemId="doc.id" :typeName="doc.typeName" :folderId="doc.folderId" :showTypeName="true"></ShowPropertyReadOnly>
      </template>
      <template v-if="dialog.title==$t('application.relationDC')">
       <RelationDocs :docId="docId"></RelationDocs>
      </template>
       <template v-if="dialog.title==$t('application.DCver')">
        <DocVersion ref="docVersion" :docId="docId"></DocVersion>
      </template>
       <template v-if="dialog.title==$t('application.Rendition')">
        <ViewRedition :docId="docId" :downloadEnable="doc.permit>=4"></ViewRedition>
      </template>
       <template v-if="dialog.title==$t('application.viewWorkflow')">
       <ViewWorkflow :docId="docId"></ViewWorkflow>
      </template>
      <template v-if="dialog.title==$t('application.TransferDoc')">
        <InTransferDoc :docId="docId"></InTransferDoc>
      </template>
      <template v-if="dialog.title==$t('application.relevantDoc')">
        <RelevantDoc :docId="docId"></RelevantDoc>
      </template>
      <template v-if="dialog.title==$t('application.Attachment')">
        <AttachmentFile :docId="docId" :allowEdit="false"></AttachmentFile>
      </template>
      <template v-if="dialog.title==$t('application.replyDoc')">
        <!-- <AttachmentFile :docId="docId"></AttachmentFile> -->
        <RevertFile :docId="docId"></RevertFile>
      </template>
      <template v-if="dialog.title=='利用信息'">
        <UseInfo :docId="docId"></UseInfo>
      </template>
       <template v-if="dialog.title==$t('application.Changeinfo')">
        <ChangeDocViewer :coding="doc.code" :revision="doc.revision"></ChangeDocViewer>
      </template>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialog.visible = false">{{$t('application.cancel')}}</el-button>
        <!--
        <el-button type="primary" @click="dialogSubmit()">{{$t('application.ok')}}</el-button>
        -->
      </span>
    </el-dialog>
  </el-container>
</template>

<script>

import ShowPropertyReadOnly from '@/components/ShowPropertyReadOnly.vue'
import Watermark from "@/assets/js/watermark.js"
import DocAttrs from './DocAttrs.vue'
import RelationDocs from './RelationDocs.vue'
import DocVersion from './DocVersion.vue'
import UseInfo from './UseInfo.vue'
import ViewRedition from './ViewRedition.vue'
import ViewWorkflow from './ViewWorkflow.vue' 

import PdfViewer from './PdfViewer.vue'
import OfficeDocViewer from './OfficeDocViewer.vue'
import ImageViewer from './ImageViewer.vue'
import VideoPlayer from './VideoPlayer.vue'
import AudioPlayer from './AudioPlayer.vue'
import InnerItemViewer from "./InnerItemViewer.vue"
import ChangeDocViewer from "./ChangeDocViewer.vue"
import CADViewer from "./CADViewer.vue"
import CADViewerHtml5 from "./CADViewerHtml5.vue"
import JTViewer from "./JTViewer2.vue"
import ThreeDsViewer from "./ThreeDsViewer copy.vue"
import Borrow from "@/components/form/Borrow.vue"
import InTransferDoc from "@/views/dc/InTransferDoc.vue"
import RelevantDoc from "@/views/dc/RelevantDoc.vue"
import AttachmentFile from "@/views/dc/AttachmentFile.vue"
import RevertFile from "@/views/dc/RevertFile.vue"
import { timeout } from 'q'

export default {
  components:{
    ShowPropertyReadOnly:ShowPropertyReadOnly,
    DocAttrs:DocAttrs,
    RelationDocs:RelationDocs,
    DocVersion:DocVersion,
    UseInfo:UseInfo,
    ViewRedition:ViewRedition,
    OfficeDocViewer:OfficeDocViewer,
    ImageViewer:ImageViewer,
    VideoPlayer:VideoPlayer,
    AudioPlayer:AudioPlayer,
    InnerItemViewer:InnerItemViewer,
    ChangeDocViewer:ChangeDocViewer,
    PdfViewer:PdfViewer,
    CADViewer:CADViewer,
    CADViewerHtml5:CADViewerHtml5,
    JTViewer:JTViewer,
    Borrow:Borrow,
    ThreeDsViewer: ThreeDsViewer,
    InTransferDoc:InTransferDoc,
    RelevantDoc:RelevantDoc,
    AttachmentFile:AttachmentFile,
    RevertFile:RevertFile
  },
  data(){
    return {
      user:{},
      token:"",
      docId:"",
      docObj:null,
      viewerType: 0,
      borrowData:[],
      ip:"",
      doc:{
        id:"",
        code:"",
        revision:"",
        title:"",
        folderId:"",
        typeName:"",
        format:"",
        permit:0,
        contentSize:0,
        hasPdf:false,
        changeCount:0,
        C_IMPORT_NAME:''
      },
      message:"加载中。。。",
      watermarkText:"",
      innerTableHeight:window.innerHeight - 75,
      dialog:{
        title:"",
        visible:false
      },
      borrowDialogVisible:false,
      revertType:"",
      judgeDownload:{
        showDownloadButton:false,
        borrowHistory:false
      },
      judgePrint:{
        printPermit:""
      },
      defaultUrl:"/dc/getDocument",
      npicUrl:"/cd/dc/getDocumentNpic"
    }
  },
  created(){
    var _self = this;
    _self.getUserIP((ip) => {
      _self.ip = ip;
    });
  },
  computed:{
    downloadEnable(){
      return this.doc.permit>=4;
    }
  },
  mounted(){
    var _self = this;
    this.docId = this.$route.query.id;
    let sysCode=this.$route.query.sysCode;
    let url=this.defaultUrl;
    if(sysCode&&sysCode=='npic'){
      url=this.npicUrl;
    }
    var user = sessionStorage.getItem("access-user");
    this.user = JSON.parse(user);
    this.token = sessionStorage.getItem("access-token");
    axios.post(url,this.docId).then(function(response) {
        _self.docObj=response.data.data;
        _self.doc.permit = response.data.permit;
        _self.doc.hasPdf = response.data.hasPdf;
        _self.doc.changeCount = response.data.changeCount;
        // console.log(_self.docObj);
        _self.doc.id=_self.docObj.ID;
        _self.judgePrint.printPermit = response.data.permit;
        _self.judgeShowDownload(_self.docObj.ID,response.data.permit);
        _self.doc.code=_self.docObj.CODING;
        _self.doc.revision=_self.docObj.REVISION;
        _self.doc.title=_self.docObj.TITLE;
        _self.doc.contentSize = _self.docObj.CONTENT_SIZE;
        _self.doc.folderId=_self.docObj.FOLDER_ID;
        if(!_self.doc.hasPdf){
          _self.doc.format=_self.docObj.FORMAT_NAME;
        }else{
          _self.doc.format ="pdf";
        }
        _self.doc.typeName= _self.docObj.TYPE_NAME;
        _self.doc.C_IMPORT_NAME=_self.docObj.C_IMPORT_NAME;
        _self.initViewerType();
        if(_self.viewerType>0 && _self.viewerType<100){
          _self.watermarkText =  sessionStorage.getItem("access-userName");
          var showText = _self.watermarkText +' '+ _self.ip
            +' '+ _self.datetimeFormat(new Date());
          Watermark.set(showText);
        }
      }).catch(function(error) {
        console.log(error);
    });
		// setInterval(function() {
		// 	var showText1 = _self.watermarkText +' '+ _self.ip
		// 		+' '+ _self.datetimeFormat(new Date());
		// 	Watermark.set(showText1);
		// }, 10000); //每10秒刷新一次  3000的单位是毫秒  
  },
  methods:{
    //office文档:1,图片：2，视频：3，音频：4
    initViewerType(){
      let _self = this;
      if(_self.doc){
        if(_self.doc.typeName == "卷盒" || _self.doc.typeName=="图册" || _self.docObj.C_ITEM_TYPE=='案卷'){
          _self.viewerType = 100;
        } else if(_self.doc.format == "pdf"){
          _self.viewerType = 1; 
        }else if(_self.doc.format == "doc" || _self.doc.format == "docx" ||
        _self.doc.format == "ppt" ||_self.doc.format == "pptx" ||
        _self.doc.format == "xls" ||_self.doc.format == "xlsx"){
          _self.viewerType = 2;
        }else if(_self.doc.format == "jpg"||_self.doc.format == "png"||
        _self.doc.format == "gif" || _self.doc.format == "bpm" ||
        _self.doc.format == "jpeg"
        ){
          _self.viewerType = 3;
        }else if(_self.doc.format == "mp4" || _self.doc.format == "ogg" ||_self.doc.format == "webm"){
          _self.viewerType = 4;
        }else if(_self.doc.format == "mp3" || _self.doc.format == "wav"){
          _self.viewerType = 5;
        }else if(_self.doc.format == "ocf"||_self.doc.format == "cad"){
          _self.viewerType = 6;
        }else if(_self.doc.format == "obj"||_self.doc.format == "jt"){
          _self.viewerType = 7;
        }else if(_self.doc.format == "3ds"||_self.doc.format == "rvm"){
          _self.viewerType = 8;
        }
      }
      //console.log(_self.viewerType);
    },
    getTypeNames(keyName) {
        let _self = this;
        axios
            .post("/dc/getParameters", keyName)
            .then(function(response) {
              _self.revertType = response.data.data.RevertType;
            }).catch(function(error) {
              console.log(error);
            });
    },
    download(){
      if(this.judgeDownload.borrowHistory==true){
        this.judgeDownload.showDownloadButton = false;
        this.revokeDocAcl(this.doc.id);
      }
      var url = "";
      if(this.doc.format == "pdf"){
        url = this.axios.defaults.baseURL+"/dc/getContent4Water?id="+this.doc.id+"&token="+sessionStorage.getItem('access-token')+"&action=download";
      }else{
        url = this.axios.defaults.baseURL+"/dc/getContent?id="+this.doc.id+"&token="+sessionStorage.getItem('access-token')+"&action=download";
      }
      
      this.recordAudit(this.doc.id);
      window.open(url, '_blank');
    },
    judgeShowDownload(docId,permitLevel){
      //有下载权限，这时候就需要判断是否本来就有下载权限
      let _self = this;
      if(permitLevel>=4){
        _self.judgeDownload.showDownloadButton = true;
        var m = new Map()
        m.set("docId",docId)
        axios
          .post("/archive/judgeDownloadByPermit", JSON.stringify(m))
          .then(function(response){
            if(response.data.code == 1){
            _self.judgeDownload.borrowHistory = response.data.borrowHistory;
            }
          })
      }else{
        _self.judgeDownload.showDownloadButton = false;
        return;
      }
    },
    recordAudit(docId){
      var m = new Map();
      m.set("docId",docId)
      m.set("actionName","ecm_download")
      m.set("appName","portal")
      axios
        .post("/archive/addAudit", JSON.stringify(m))
        .then(function(response){
          
        })
    },  
    revokeDocAcl(docId){
      var m = new Map();
      m.set("docId",docId)
      axios
        .post("/archive/revokeAcl", JSON.stringify(m))
        .then(function(response){})
    },
    menuClick(type){
      console.log(this.$t('application.dcproper'))
      this.dialog.title=type;
      this.dialog.visible=true;
      if(type=='文档版本'){
         setTimeout(() => {
           if(this.$refs.docVersion){
             this.$refs.docVersion.loadData();
           }
        }, 100);
      }
    },
    handleClose(done){
      this.dialog.visible = false
    },
    dialogSubmit(){
      if(this.dialog.title==this.$t('application.dcproper') && this.currentUser().systemPermission>5){
        this.$refs.ShowProperty.saveItem();
        
        this.dialog.visible = false
      }else if(this.dialog.title=='借阅'){
        this.$message("借阅");
      }else{
        this.dialog.visible = false
      }
    },
    getUserIP(onNewIP) {
        let MyPeerConnection = window.RTCPeerConnection || window.mozRTCPeerConnection || window.webkitRTCPeerConnection;
        let pc = new MyPeerConnection({
            iceServers: []
          });
        let noop = () => {
          };
        let localIPs = {};
        let ipRegex = /([0-9]{1,3}(\.[0-9]{1,3}){3}|[a-f0-9]{1,4}(:[a-f0-9]{1,4}){7})/g;
        let iterateIP = (ip) => {
          if (!localIPs[ip]) onNewIP(ip);
          localIPs[ip] = true;
        };
        pc.createDataChannel('');
        pc.createOffer().then((sdp) => {
          sdp.sdp.split('\n').forEach(function (line) {
            if (line.indexOf('candidate') < 0) return;
            line.match(ipRegex).forEach(iterateIP);
          });
          pc.setLocalDescription(sdp, noop, noop);
        }).catch((reason) => {
        });
        pc.onicecandidate = (ice) => {
          if (!ice || !ice.candidate || !ice.candidate.candidate || !ice.candidate.candidate.match(ipRegex)) return;
          ice.candidate.candidate.match(ipRegex).forEach(iterateIP);
        };
      },
      borrowItem(obj) {
        let _self = this;
       
        if(typeof(obj.C_ARCHIVE_UNIT)=="undefined"){
              _self.$message({
                showClose: true,
                message: _self.$t('message.EmptyPlaceOnFile'),
                duration: 5000,
                type: "warning"
              });
              return;
        }
        _self.borrowData=[];
        
        _self.borrowData.push(obj);
        _self.borrowDialogVisible=true;
      },
    showOrHiden(b){
      this.borrowDialogVisible=b;
    },

  }
}
</script>

<style scoped>
.aside-rigth .el-button{
  margin-bottom: 5px;
  margin-left: 5px;
}
.doccontent{
  min-height: 640px;
  border-style:solid;
	border-width:1px;
  border-color: grey;
}
.el-header{
  color: white;
  background-color: #36a9e1;
  padding-top: 15px;
  padding-left: 15px;
}

.el-main {
    display: block;
    -webkit-box-flex: 1;
    -ms-flex: 1;
    flex: 1;
    -ms-flex-preferred-size: auto;
    flex-basis: auto;
    overflow: auto;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    padding: 0px;
}
</style>