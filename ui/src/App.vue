<template>
  <div id="app">
    <header class="navbar">
      <section class="navbar-center"></section>
      <section class="navbar-section m-2">
        <a alt="About" href="#" class="btn btn-link" @click="isActive = true"
          >About</a
        >
        <a
          alt="GitHub Repo"
          class="btn btn-link"
          href="https://github.com/ArqiSoft/chemical-search-service"
          target="_blank"
          rel="noopener noreferrer"
        >
          <img
            alt="ArqiSoft logo"
            src="./assets/images/github-brands.svg"
            width="24"
            title="GitHub Repo"
          />
        </a>
      </section>
    </header>
    <a
      alt="ArqiSoft, LLC"
      href="http://www.arqisoft.com/"
      target="_blank"
      rel="noopener noreferrer"
    >
      <img alt="ArqiSoft logo" src="./assets/images/logo.png" class="logo" />
    </a>
    <Search header="Chemical Search" />

    <div class="modal" id="modal-id" :class="{ active: isActive }">
      <a href="#close" class="modal-overlay" aria-label="Close"></a>
      <div class="modal-container">
        <div class="modal-header">
          <a
            href="#close"
            class="btn btn-clear float-right"
            aria-label="Close"
            @click="isActive = false"
          ></a>
          <div class="modal-title h5">About</div>
        </div>
        <div class="modal-body">
          <div class="content">
            <p>
              Chemical Search is a demo application to evaluate chemical
              structure searches performance running
              <a
                alt="Bingo API with ElasticSearch"
                href="https://github.com/epam/Indigo/tree/master/api/plugins/bingo-elastic/java"
                target="_blank"
                rel="noopener noreferrer"
                >Bingo API with ElasticSearch</a
              >. This application has been built for research purposes only and
              can be stopped any time.
            </p>
            <p>
              Publicly available
              <a
                alt="SureChEMBL dataset"
                href="ftp://ftp.ebi.ac.uk/pub/databases/chembl/SureChEMBL/data/"
                target="_blank"
                rel="noopener noreferrer"
                >SureChEMBL dataset</a
              >
              is preloaded into ElasticSearch and current index contains more
              than 20 millions compounds.
            </p>
            <p>
              The application hosted in AWS environment on a separate EC2
              instance with ElasticSearch 7 running in Docker but some other
              configurations may be applied later.
            </p>
            <p>
              <a
                alt="GitHub Repo"
                href="https://github.com/ArqiSoft/chemical-search-service"
                target="_blank"
                rel="noopener noreferrer"
                >Source code</a
              >
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import Search from './components/Search.vue';

@Component({
  components: {
    Search,
  },
})
export default class App extends Vue {
  isActive = false;
}
</script>

<style lang="scss">
@import './assets/styles/styles';

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
}

.logo {
  width: 10rem;
  text-align: center;
}

#modal-id {
  text-align: left;
}
</style>
