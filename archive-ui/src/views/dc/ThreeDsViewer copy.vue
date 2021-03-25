<template>
  <iframe key="#1" :src="objUrl" frameborder="0" width="100%" :height="itemHeight" ></iframe>
</template>

<script type="text/javascript">
import { ModelObj } from 'vue-3d-model'
export default {
  name: "ThreeDsViewer",
  components: {
            ModelObj
        },
  data() {
    return {
		itemHeight: window.innerHeight - 50,
		objUrl:""
    };
  },
  computed: {},
  props: {
    id:{type:String},
    format:{type:String}
  },
  created() {
	if(this.id==null && this.$route.query.id){
      this.id = this.$route.query.id;
    }
    if(this.format==null && this.$route.query.format){
      this.format = this.$route.query.format;
    }
    if(!isIE()){
      this.loadUrl();
      this.writeAudit(this.id);
    }
  },
  methods: {
	  loadUrl() {
      let _self = this;
      let getfileUrl =  _self.axios.defaults.baseURL+"/dc/getContent?id="+_self.id+"&token="+sessionStorage.getItem('access-token')+"&format=obj";
      _self.objUrl = "./static/threeviewer/threeviewer.html?file="+encodeURIComponent(getfileUrl)
	},
  isIE(){
    if (window.navigator.userAgent.indexOf("MSIE")>=1) 
      return true; 
    else
      return false; 
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
