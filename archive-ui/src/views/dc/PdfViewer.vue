<template>
    <iframe key="#1" :src="pdfUrl" frameborder="0" width="100%" :height="itemHeight" ></iframe>
</template>

<script type="text/javascript">
import 'url-search-params-polyfill'

export default {
  name: "PdfViewer",
  data() {
    return {
      pdfUrl:"",
      itemHeight: window.innerHeight - 50,
      //判断是否释放打印按钮
      judgePrint:{
        showPrintButton:false,
        borrowHistory:false
      }
    };
  },
  props: {
    id:{type:String},
    format:{type:String},
    permitLevel:{type:String}
  },
  created() {
    if(this.id==null && this.$route.query.id){
      this.id = this.$route.query.id;
    }
    if(this.format==null && this.$route.query.format){
      this.format = this.$route.query.format;
    }
    this.judgeShowPermit(this.id,this.permitLevel)
    this.loadUrl();
    this.writeAudit(this.id)
  },
  methods: {
    loadUrl(hasBorrowHistory) {
      let _self = this;
      var showPrintButton = _self.judgePrint.showPrintButton;
      var borrowHistory = hasBorrowHistory;
      var baseURL = _self.axios.defaults.baseURL;
      var params = "&showPrintButton="+showPrintButton+"&borrowHistory="+borrowHistory
      let getfileUrl =  _self.axios.defaults.baseURL+"/dc/getContent4Water?id="+_self.id+"&token="+sessionStorage.getItem('access-token')+"&format=pdf";
      let auditUrl = _self.axios.defaults.baseURL+"/archive/addAudit2?docId="+_self.id+"&actionName=ecm_print"+"&appName=portal"
      let invokeUrl = _self.axios.defaults.baseURL+"/archive/revokeAcl2?docId="+_self.id
      _self.pdfUrl = "./static/pdfviewer/web/viewer.html?file="+encodeURIComponent(getfileUrl)+"&.pdf"+params+"&audit="+encodeURIComponent(auditUrl)+"&invoke="+encodeURIComponent(invokeUrl)
    },
    writeAudit(docId){
      var m = new Map();
      m.set("docId",docId)
      m.set("actionName","ecm_read")
      m.set("appName","portal")
      axios
        .post("/audit/addAudit", JSON.stringify(m))
        .then(function(response){
          
        })
    },
    judgeShowPermit(docId,permitLevel){
      //有打印权限，这时候就需要判断是否本来就有打印权限
      let _self = this;
      if(permitLevel>=4){
        _self.judgePrint.showPrintButton = true;
        var m = new Map()
        m.set("docId",docId)
       axios
          .post("/archive/judgePrintByPermit", JSON.stringify(m))
          .then(function(response){
            if(response.data.code == 1){
            _self.judgePrint.borrowHistory = response.data.borrowHistory;
            _self.loadUrl(_self.judgePrint.borrowHistory);
            }
          })
      }else{
        _self.judgePrint.showPrintButton = false;
        _self.loadUrl(false);
      }
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1,
h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
