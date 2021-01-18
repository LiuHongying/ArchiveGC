<template>
  <div>

     <div style="display:inline-block;position: absolute;left:500px;">
        <button @click="printCode" v-print="'#print'">打印</button>
      </div>
      <div id='print' ref='print' :style="'position: absolute; top:0px;'">
        <div v-for="(item,idx) in archiveObjects" :key="'divk'+idx" 
        :style="'width:'+divWidth+';height:'+divHeight+';text-align:center;margin:'+divMargin+';line-height:'+divHeight+';font-size:'+fontSize+';'">
          <span>档 号 &nbsp; {{item.C_ARCHIVE_CODING}}</span>
          <div style="page-break-before:always;"></div>
        </div>
        
  　　</div>
  </div>
</template>

<script type="text/javascript">
import Print from '@/plugins/print'
import Vue from 'vue';
Vue.use(Print)
export default {
   name: 'printArchiveCode',
    
  // name: "printPage",
  data() {
    return {
      archiveTitle:"",
      archiveCode:"",
      innerDataList:[],
      dialogQrcodeVisible: true,
      currentLanguage: "zh-cn",
      volumeTitle:"",
      ridgeData:[],
      selectedPx:'112',
      barCodeWidth:2,
      barCodeHeight:40,
    };
  },
  mounted() {
    // 需要先显示出来，然后再隐藏掉；  否则动态生成的二维码，第一次会报错，对象找不到。可能是跟初始化有关系，没有显示出来的时候并没有初始化HTML
      // this.dialogQrcodeVisible = false
    this.currentLanguage = localStorage.getItem("localeLanguage") || "zh-cn";
    //this.getConfigParam("PrintArchiveCodeConfig");
    // this.loadFormInfo();
    // this.getArchiveObj(this.archiveId,this.gridName); 
    
  },
  props: {
    archiveObjects:{type:Array,default:() => []},
    divWidth:{type:String,default:'90mm'},
    divHeight:{type:String,default:'10mm'},
    divMargin:{type:String,default:'4px'},
    fontSize:{type:String,default:'18px'},
  },
  methods: {
      getConfigParam(keyName) {
        let _self = this;
        axios
          .post("/dc/getJsonParamMap", keyName)
          .then(function(response) {
            //_self.divWidth = response.data.data.divWidth;
           // _self.divHeight = response.data.data.divHeight;
            //_self.divMargin = response.data.data.divMargin;
          })
          .catch(function(error) {
            console.log(error);
          });
      },
      refresh(objs,pixel){
        let _self=this;
        _self.archiveObjects=objs;
        
        
      },
    
    printCode(){
      this.$print(this.$refs.print);
    },

  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style media="printContent" scoped>
@page {
size: auto; /* auto is the initial value /
margin: 3mm; / this affects the margin in the printer settings */
}

html {
background-color: #ffffff;
margin: 0px; /* this affects the margin on the html before sending to printer */
}

body {
border: solid 1px rgba(255,255,255,0);
/* margin: 10mm 15mm 10mm 15mm; margin you want for the content */
}
</style>
