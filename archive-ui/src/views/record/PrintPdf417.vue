<template>
  <div>
      <!-- <div style="width:30%;display:inline-block;position: absolute; left:30px;">
        <el-select
            name="selectName"
            v-model="selectedPx"
            placeholder="'请选择条码尺寸'"
            style="display:block;"
            @change="refresh(archiveObjects,selectedPx)"
            >
            <el-option label="1条码" value="1" key='2'></el-option>
            <el-option label="2条码" value="2" key='2'></el-option>
            <el-option label="3条码" value="3" key='3'></el-option>
            <el-option label="4条码" value="4" key='4'></el-option>
            <el-option label="5条码" value="5" key='5'></el-option>
            <el-option label="6条码" value="6" key='6'></el-option>
          </el-select>
          
      </div> -->
      <div style="display:inline-block">
        <el-row>
          <el-select v-model="printType" @change="onPrintTypeChange" >
            <template v-for="item in printTypeList">
                <el-option :label="item" :value="item" :key="item"></el-option>
            </template>
          </el-select>
          <button @click="printCode" v-print="'#print'">打印</button>
        </el-row>
      </div>
     <el-container style="width:100%;height:540px;overflow:auto;">
      <div id='print' ref='print' :style="'top:0px;'">
        <div v-for="(item,keys) in printObjects" :key="'divk'+keys" :style="'width:'+divWidth+';padding:5px;'">
          <el-row>
            <el-col :span="12" style="color: #000000;text-align: left;font-size:16px;padding:4px;">{{item.archiveClassic}}</el-col>
            <el-col :span="6" style="color: #000000;text-align: left;font-size:16px;padding:4px;">{{item.itemType}}</el-col>
            <el-col :span="6" style="color: #000000;text-align: left;font-size:16px;padding:4px;">{{printType}}</el-col>
          </el-row>
          <el-row>
            <el-col :span="24" style="color: #000000;text-align: left;font-size:28px;padding:4px;">{{item.coding}}&nbsp;</el-col>
          </el-row>
          <el-row>
            <el-col :span="12" style="color: #000000;text-align: left;font-size:16px;padding:4px;">密级：{{item.securityLevel}}</el-col>
            <el-col v-if="item.revision" :span="12" style="color: #000000;text-align: left;font-size:16px;padding:4px;">版本：{{item.revision}}</el-col>
          </el-row>
          <el-row>
            <el-col :span="12" style="color: #000000;text-align: left;font-size:16px;padding:4px;">保管期限：{{item.retention}}</el-col>
            <el-col v-if="item.archiveClassic=='科技与信息'" :span="12" style="color: #000000;text-align: left;font-size:16px;padding:4px;">项目代码：{{item.projectCode}}</el-col>
            <el-col v-else :span="12" style="color: #000000;text-align: left;font-size:16px;padding:4px;">工程号：{{item.projectCode}}</el-col>
          </el-row>
          <el-row>
            <el-col :span="24" style="color: #000000;text-align: left;font-size:16px;padding:4px;">归档日期：{{item.archiveDate}}</el-col>
          </el-row>
          <el-row style="padding-bottom:15px;">
            <el-col :span="10" style="padding-top:10px;">
              <el-row style="color: #000000;text-align: left;font-size:16px;padding:2px;">{{item.volString}}</el-row>
              <el-row style="color: #000000;text-align: center;font-size:46px;padding-top:10px;">{{item.storeCoding}}</el-row>
            </el-col>
            <el-col :span="14">
              <img width="100%" :src="_self.axios.defaults.baseURL+'/record/print/getContentBarcode?str='+item.id +';' +item.archiveCoding + ';'+item.coding+';'+item.revision+';'+'&token='+token+'&ticket='+ticket+'_'+keys" border="0" />
            </el-col>
          </el-row>
          <div v-if="keys < printObjects.length-1" style="page-break-before:always;"></div>
        </div>
        <!-- <div v-if="isQRCode"  ref='qrCodeUrl2'></div> -->
  　　</div>
     </el-container>
  </div>
</template>

<script type="text/javascript">
import Print from '@/plugins/print'
import PDF417 from '@/plugins/pdf417'
import Vue from 'vue';
import QRCode from 'qrcodejs2'// 引入qrcode
import JsBarcode from 'jsbarcode'
import MainContainer from '../MainContainer.vue';
Vue.use(Print)
Vue.use(PDF417);
export default {
  components: { MainContainer },
   name: 'printPDF147Code',
    
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
      noneStr:"block",
      barCodeWidth:2,
      barCodeHeight:40,
      printType:"原件",
      printTypeListGeneral:["原件","复制件"],
      printTypeListBussiness:["正本","副本","复制件"],
      printTypeList:[],
      printObjects:[],
      token:"",
      ticket:100,
    };
  },
  mounted() {
    let _self = this;
    _self.printTypeList = _self.printTypeListGeneral;
    _self.currentLanguage = localStorage.getItem("localeLanguage") || "zh-cn";
    _self.getConfigParam("PrintArchiveCodeConfig");
    _self.token = sessionStorage.getItem('access-token');
  },
  props: {
    archiveObjects:{type:Array,default:() => []},
    divWidth:{type:String,default:'400px'},
    divHeight:{type:String,default:'50px'},
    divMargin:{type:String,default:'10px'},
  },
  methods: {
    loadData(data){
      let _self = this;
      if(data){
        _self.archiveObjects = data;
      }
      let isbusiness = false;
      for(let i=0;i< _self.archiveObjects.length;i++){
        let obj = _self.archiveObjects[i];
        if(obj["C_ARC_CLASSIC"] && obj["C_ARC_CLASSIC"]=="商务管理"){
          _self.printTypeList = _self.printTypeListBussiness;
          _self.printType ="正本";
          isbusiness = true;
          break;
        }
      }
      if(!isbusiness){
        _self.printTypeList = _self.printTypeListGeneral;
        _self.printType ="原件";
      }
      setTimeout(() => {
        _self.getPrintObjects();
        }, 100
      );
    },
      getConfigParam(keyName) {
        let _self = this;
        axios
          .post("/dc/getJsonParamMap", keyName)
          .then(function(response) {
            _self.divWidth = response.data.data.divWidth;
            _self.divHeight = response.data.data.divHeight;
            _self.divMargin = response.data.data.divMargin;
          })
          .catch(function(error) {
            console.log(error);
          });
      },
      onPrintTypeChange(val){
        this.printType = val;
        this.getPrintObjects();
      },

    getPrintObjects(){
      let _self=this;
      var m = new Map();
      _self.loading = true;
      let ids = [];
      for(let i=0;i< _self.archiveObjects.length;i++){
        let obj = _self.archiveObjects[i];
        ids.push(obj["ID"]);
      }
      m.set('ids',ids);//ID
      m.set('printType',_self.printType);
      _self.axios.post("/record/print/getPrintData", JSON.stringify(m))
        .then(function(response) {
          _self.printObjects = response.data.data;
          _self.ticket ++;
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    printCode(){
      this.noneStr = "none";
      setTimeout(() => {
      this.$print(this.$refs.print);
    }, 200);
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
