<template>
  <div>
      <div style="width:30%;display:inline-block;position: absolute; left:30px;">
        <el-select
            name="selectName"
            v-model="selectedPx"
            placeholder="'请选择条码尺寸'"
            style="display:block;"
            @change="refreshBarCode(archiveObjects,selectedPx)"
            >
            <el-option label="1条码" value="1" key='2'></el-option>
            <el-option label="2条码" value="2" key='2'></el-option>
            <el-option label="3条码" value="3" key='3'></el-option>
            <el-option label="4条码" value="4" key='4'></el-option>
            <el-option label="5条码" value="5" key='5'></el-option>
            <el-option label="6条码" value="6" key='6'></el-option>
          </el-select>
          
      </div>
      <div style="display:inline-block;position: absolute;left:210px;">
        <button @click="printCode" v-print="'#print'">打印</button>
      </div>
      <div id='print' ref='print' :style="'position: absolute; top:130px;'">
        <div v-for="(item,keys) in getArchiveObjs()" :key="'divk'+keys">
          <img :id="'barcode'+keys" :key="'itmk'+keys" />
        </div>
        <!-- <div v-if="isQRCode"  ref='qrCodeUrl2'></div> -->
  　　</div>
  </div>
</template>

<script type="text/javascript">
import Print from '@/plugins/print'
import Vue from 'vue';
import QRCode from 'qrcodejs2'// 引入qrcode
import JsBarcode from 'jsbarcode'
Vue.use(Print)
export default {
   name: 'test',
    
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
    
    // this.loadFormInfo();
    // this.getArchiveObj(this.archiveId,this.gridName); 
    
  },
  props: {
    archiveId: {type:[String,Number]},
    currentFolderId:{type:[String,Number]},
    tableHeight:{type:Number},
    gridName:{type:String},
    isQRCode:{type:Boolean,default:false},
    isBarCode:{type:Boolean,default:false},
    archiveObjects:{type:Array,default:() => []},
  },
  methods: {
      getArchiveObjs(){
        return this.archiveObjects;
      },
      refreshBarCode(objs,pixel){
        let _self=this;
        _self.archiveObjects=objs;
        for(let i=0;i<objs.length;i++){
          _self.genarateBarCode('#barcode'+i,objs[i].CODING,pixel,_self.barCodeHeight);
        }
        
      },
    getArchiveObj(id,volumeTitle){
      let _self=this;
      _self.volumeTitle=volumeTitle;
      var m = new Map();
      m.set('itemInfo',id);//ID 或类型
      m.set('lang',_self.currentLanguage);
      _self.axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(m),//_self.myItemId+_self.myTypeName,
          url: "/dc/getArchiveObj"
        })
        .then(function(response) {
          _self.ridgeData=response.data.data;
          _self.archiveCode= response.data.data.coding;
          _self.archiveTitle= response.data.data.title;
          // _self.genarateQrcode(_self.archiveCode);
          _self.genarateBarCode('#barcode0',_self.archiveCode,_self.barCodeWidth,_self.barCodeHeight);
          // _self.InnerFile();
          //console.log(JSON.stringify(response.data.data));
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    printCode(){
      this.$print(this.$refs.print);
    },
    
    checkEwmClick (url) {
      let vm = this
      vm.$nextTick(() => {
        vm.dialogQrcodeVisible = true
        let obj = document.getElementById('qrcodeshow')
        obj.innerHTML = ''
        vm.genarateQrcode(url)
      })
    },
    handleDialogQrcodeClose () {
      this.dialogQrcodeVisible = false
    },
    genarateBarCode(elId,value,width,height){
      JsBarcode(elId, value, {
        format: 'CODE39',
        lineColor: '#000',
        background: '#EBEEF5',
        width: 1,//2
        height: height,//40
        displayValue: true
      })
    },
    genarateQrcode (url) {
      this.$refs.qrCodeUrl2.innerHTML='';
      let qrcode = new QRCode(this.$refs.qrCodeUrl2, {
        width: 50,
        height: 50,
        text: url, // 二维码地址
        colorDark: '#000',
        colorLight: '#fff',
        correctLevel: QRCode.CorrectLevel.H
      })
      console.log('qrcode = ' + JSON.stringify(qrcode))
    }
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
